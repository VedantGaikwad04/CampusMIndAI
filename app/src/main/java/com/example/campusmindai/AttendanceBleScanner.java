package com.example.campusmindai;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Scans for the CampusMind teacher attendance BLE advertisement.
 * Proximity is estimated using RSSI; this is not an exact distance measurement.
 */
public class AttendanceBleScanner {

    private static final int MIN_RSSI = -75;
    private static final long WEAK_SIGNAL_CALLBACK_INTERVAL = 1500L;

    private final Context context;
    private final Listener listener;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private boolean scanning = false;
    private long lastWeakSignalCallbackMillis = 0L;

    public interface Listener {
        void onTeacherDetected(String attendanceCode, int rssi);
        void onScanStarted();
        void onSignalTooWeak(int rssi);
        void onScanError(String message);
    }

    public AttendanceBleScanner(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;

        BluetoothManager manager =
                (BluetoothManager) this.context.getSystemService(
                        Context.BLUETOOTH_SERVICE
                );

        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
        }
    }

    public boolean startScanning() {

        if (bluetoothAdapter == null) {
            notifyError("Bluetooth is not supported on this device.");
            return false;
        }

        if (!hasRequiredPermission()) {
            notifyError("Bluetooth scan permission is required.");
            return false;
        }

        try {
            if (!bluetoothAdapter.isEnabled()) {
                notifyError("Please turn on Bluetooth and try again.");
                return false;
            }

            scanner = bluetoothAdapter.getBluetoothLeScanner();

            if (scanner == null) {
                notifyError("Unable to access the Bluetooth scanner.");
                return false;
            }

            if (scanning) {
                stopScanning();
            }

            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(
                            new ParcelUuid(AttendanceBleManager.SERVICE_UUID)
                    )
                    .build();

            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();

            scanner.startScan(filters, settings, scanCallback);
            scanning = true;

            if (listener != null) {
                listener.onScanStarted();
            }

            return true;

        } catch (SecurityException e) {
            notifyError("Bluetooth permission was denied.");
            return false;

        } catch (Exception e) {
            notifyError("Unable to start Bluetooth scan.");
            return false;
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result == null || !hasRequiredPermission()) {
                return;
            }

            ScanRecord record = result.getScanRecord();
            if (record == null) {
                return;
            }

            byte[] serviceData;
            try {
                serviceData = record.getServiceData(
                        new ParcelUuid(AttendanceBleManager.SERVICE_UUID)
                );
            } catch (SecurityException e) {
                notifyError("Bluetooth scan permission was denied.");
                return;
            }

            if (serviceData == null || serviceData.length == 0) {
                return;
            }

            String code = new String(
                    serviceData,
                    StandardCharsets.UTF_8
            ).trim();

            if (!isSixDigitCode(code)) {
                return;
            }

            int rssi = result.getRssi();

            if (rssi < MIN_RSSI) {
                long now = System.currentTimeMillis();
                if (listener != null
                        && now - lastWeakSignalCallbackMillis >= WEAK_SIGNAL_CALLBACK_INTERVAL) {
                    lastWeakSignalCallbackMillis = now;
                    listener.onSignalTooWeak(rssi);
                }
                return;
            }

            if (listener != null) {
                listener.onTeacherDetected(code, rssi);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            scanning = false;
            notifyError("Bluetooth scan failed (" + errorCode + "). Check Bluetooth and try again.");
        }
    };

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

    public void stopScanning() {

        if (!scanning || scanner == null) {
            return;
        }

        try {
            if (hasRequiredPermission()) {
                scanner.stopScan(scanCallback);
            }
        } catch (SecurityException ignored) {
            // The permission may have been revoked while scanning.
        }

        scanning = false;
    }

    public boolean isScanning() {
        return scanning;
    }

    private boolean hasRequiredPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void notifyError(String message) {

        scanning = false;

        if (listener != null) {
            listener.onScanError(message);
        }
    }

    public void release() {
        stopScanning();
        scanner = null;
        bluetoothAdapter = null;
    }
}
