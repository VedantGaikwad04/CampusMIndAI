package com.example.campusmindai;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Advertises the active teacher attendance code over BLE.
 * Uses a 16-bit Bluetooth-base service UUID so the service UUID plus six-digit
 * service data can fit within a legacy BLE advertising packet.
 */
public class AttendanceBleManager {

    private static final String TAG = "CampusMindBLE";

    public interface StatusListener {
        void onStatusChanged(String message, boolean advertising);
    }

    private StatusListener statusListener;

    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
    }

    private void notifyStatus(String message, boolean advertising) {
        Log.d(TAG, message);
        StatusListener listener = statusListener;
        if (listener != null) {
            listener.onStatusChanged(message, advertising);
        }
    }

    public static final UUID SERVICE_UUID =
            UUID.fromString("0000FDEA-0000-1000-8000-00805F9B34FB");

    private final Context context;

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertiseCallback;
    private ListenerRegistration sessionListener;
    private String requestedCode;

    public AttendanceBleManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // -------------------------------------------------------------
    // WATCH ACTIVE SESSION(S) OWNED BY THIS TEACHER
    // -------------------------------------------------------------

    public void startSessionWatcher() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            stopSessionWatcher();
            notifyStatus("Sign in before starting Bluetooth attendance.", false);
            return;
        }

        String teacherUid = auth.getCurrentUser().getUid();
        stopSessionWatcher();

        sessionListener = firestore.collection("attendanceSessions")
                .whereEqualTo("teacherUid", teacherUid)
                .whereEqualTo("active", true)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {
                        stopAdvertising();
                        Log.e(TAG, "Failed to read attendance sessions", error);
                        notifyStatus("Firestore error while checking session: "
                                + error.getCode().name(), false);
                        return;
                    }

                    if (snapshot == null || snapshot.isEmpty()) {
                        stopAdvertising();
                        notifyStatus("Waiting for an active attendance session.", false);
                        return;
                    }

                    String activeCode = null;
                    long newestCreatedAt = Long.MIN_VALUE;

                    for (com.google.firebase.firestore.DocumentSnapshot document
                            : snapshot.getDocuments()) {

                        String candidateCode = document.getString("code");
                        Long createdAt = document.getLong("createdAtMillis");

                        if (candidateCode == null || !isSixDigitCode(candidateCode)) {
                            continue;
                        }

                        long candidateTime = createdAt == null ? 0L : createdAt;
                        if (activeCode == null || candidateTime >= newestCreatedAt) {
                            activeCode = candidateCode;
                            newestCreatedAt = candidateTime;
                        }
                    }

                    if (activeCode == null) {
                        stopAdvertising();
                        notifyStatus("Active session found, but its 6-digit code is missing or invalid.", false);
                        return;
                    }

                    if (activeCode.equals(requestedCode)) {
                        return;
                    }

                    stopAdvertising();
                    startAdvertising(activeCode);
                });
    }

    // -------------------------------------------------------------
    // START BLE ADVERTISEMENT
    // -------------------------------------------------------------

    public boolean startAdvertising(String attendanceCode) {

        if (!isSixDigitCode(attendanceCode)) {
            notifyStatus("The attendance code is not a valid 6-digit code.", false);
            return false;
        }

        if (!hasAdvertisePermissions()) {
            notifyStatus("Bluetooth advertise/connect permission is missing. Allow Nearby Devices permission.", false);
            return false;
        }

        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if (adapter == null) {
                notifyStatus("This phone does not have a Bluetooth adapter.", false);
                return false;
            }

            if (!adapter.isEnabled()) {
                notifyStatus("Bluetooth is turned off on the teacher phone. Turn it on.", false);
                return false;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                notifyStatus("BLE advertising requires Android 5.0 or newer.", false);
                return false;
            }

            // Do not gate basic advertising on isMultipleAdvertisementSupported();
            // that reports support for multiple advertising instances, not all
            // single-advertisement capability. The advertiser object is the real check.
            advertiser = adapter.getBluetoothLeAdvertiser();
            if (advertiser == null) {
                notifyStatus("This teacher phone does not support BLE advertising, or the Bluetooth adapter is unavailable.", false);
                return false;
            }

            byte[] codeBytes = attendanceCode.getBytes(StandardCharsets.UTF_8);

            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(false)
                    .setTimeout(0)
                    .build();

            // With the short Bluetooth-base UUID this remains within the
            // legacy advertising payload limit while carrying the code.
            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                    .addServiceData(new ParcelUuid(SERVICE_UUID), codeBytes)
                    .build();

            requestedCode = attendanceCode;

            advertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    notifyStatus("Bluetooth signal is broadcasting.", true);
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    requestedCode = null;
                    String reason;
                    switch (errorCode) {
                        case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                            reason = "BLE advertising data is too large.";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                            reason = "The phone has no advertising slot available. Close other BLE broadcasting apps.";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                            reason = "This BLE advertisement was already started.";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                            reason = "This phone does not support the requested BLE advertising feature.";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                            reason = "Android reported an internal BLE advertising error.";
                            break;
                        default:
                            reason = "BLE advertising failed with error code " + errorCode + ".";
                            break;
                    }
                    Log.e(TAG, reason + " Code=" + errorCode);
                    notifyStatus(reason, false);
                }
            };

            advertiser.startAdvertising(settings, data, advertiseCallback);
            return true;

        } catch (SecurityException e) {
            requestedCode = null;
            Log.e(TAG, "Bluetooth permission/security exception while advertising", e);
            notifyStatus("Bluetooth permission was denied or revoked. Check Nearby Devices permission.", false);
            return false;
        } catch (Exception e) {
            requestedCode = null;
            Log.e(TAG, "Unexpected BLE advertising error", e);
            notifyStatus("BLE advertising error: " + e.getClass().getSimpleName(), false);
            return false;
        }
    }

    // -------------------------------------------------------------
    // STOP ADVERTISEMENT
    // -------------------------------------------------------------

    public void stopAdvertising() {

        if (advertiser != null && advertiseCallback != null
                && hasAdvertisePermissions()) {
            try {
                advertiser.stopAdvertising(advertiseCallback);
            } catch (SecurityException ignored) {
                // Permission may have been revoked while active.
            }
        }

        advertiser = null;
        advertiseCallback = null;
        requestedCode = null;
    }

    // -------------------------------------------------------------
    // STOP SESSION WATCHER
    // -------------------------------------------------------------

    public void stopSessionWatcher() {

        if (sessionListener != null) {
            sessionListener.remove();
            sessionListener = null;
        }

        stopAdvertising();
    }

    private boolean hasAdvertisePermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isSixDigitCode(String code) {

        if (code == null || code.length() != 6) {
            return false;
        }

        for (int i = 0; i < code.length(); i++) {
            if (!Character.isDigit(code.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
