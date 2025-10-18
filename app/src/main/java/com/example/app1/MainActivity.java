package com.example.app1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_REAL_DEVICE_ID = "real_device_id";

    private static final int ANDROID_ID_LENGTH = 16;

    private TextView tvStatus;
    private Button btnCheckPermissionID, btnChangeSystemAndroidID, btnRealDeviceID, btnReSaveRealDeviceID;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initRealDeviceID();

        btnCheckPermissionID.setOnClickListener(view -> requestRootPermission());
        btnChangeSystemAndroidID.setOnClickListener(view -> changeSystemAndroidID());
        btnRealDeviceID.setOnClickListener(view -> displayRealAndroidID());
        btnReSaveRealDeviceID.setOnClickListener(view -> restoreSavedAndroidID());
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        btnCheckPermissionID = findViewById(R.id.btnCheckPermissionID);
        btnChangeSystemAndroidID = findViewById(R.id.btnChangeSystemID);
        btnRealDeviceID = findViewById(R.id.btnRealDeviceID);
        btnReSaveRealDeviceID = findViewById(R.id.btnReSaveRealDeviceID);
    }

    private void initRealDeviceID() {
        if (!sharedPreferences.contains(KEY_REAL_DEVICE_ID)) {
            String realDeviceId = getRealAndroidID();
            sharedPreferences.edit().putString(KEY_REAL_DEVICE_ID, realDeviceId).apply();
            updateStatus(String.format("Lần đầu lưu Real Android ID: %s", realDeviceId));
        } else {
            String savedId = sharedPreferences.getString(KEY_REAL_DEVICE_ID, "");
            updateStatus(String.format("Real Android ID đã lưu: %s", savedId));
        }
    }

    private void displayRealAndroidID() {
        String realDeviceID = getRealAndroidID();
        updateStatus(String.format("Real Android ID: %s", realDeviceID));
    }

    private void restoreSavedAndroidID() {
        String savedAndroidID = sharedPreferences.getString(KEY_REAL_DEVICE_ID, null);

        if (savedAndroidID == null) {
            showToast("Chưa có Android ID nào được lưu");
            updateStatus("Chưa có Android ID nào được lưu");
            return;
        }

        updateStatus("Android ID thực đã lưu: " + savedAndroidID);
        showToast("Đã hiển thị Real Android ID đã lưu.");

        boolean success = runRootCommand("settings put secure android_id " + savedAndroidID);
        if (success) {
            updateStatus("Đã đặt lại Android ID hệ thống thành: " + savedAndroidID);
            showToast("Đã đặt lại Android ID hệ thống thành công");
        } else {
            updateStatus("Thất bại khi đặt lại Android ID hệ thống, kiểm tra quyền root");
            showToast("Không thể đổi Android ID, cần quyền root");
        }
    }

    private void changeSystemAndroidID() {
        String newAndroidID = generateNewDeviceID().replace("-", "").substring(0, ANDROID_ID_LENGTH);
        String command = "settings put secure android_id " + newAndroidID;

        boolean success = runRootCommand(command);

        if (success) {
            updateStatus("Đã đổi Android ID hệ thống thành: " + newAndroidID);
            showToast("Thay đổi Android ID thành công");
        } else {
            updateStatus("Thay đổi Android ID thất bại, kiểm tra quyền root");
            showToast("Không thể thay đổi Android ID, app cần quyền root");
        }
    }

    private void requestRootPermission() {
        boolean hasRoot = testRootAccess();
        if (hasRoot) {
            showToast("Đã có quyền ROOT!");
        } else {
            showToast("Không có quyền ROOT hoặc bị từ chối.");
        }
    }

    // ================== UTILS ===================

    private String generateNewDeviceID() {
        return UUID.randomUUID().toString();
    }

    private String getRealAndroidID() {
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }


    private boolean runRootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeQuietly(os);
            if (process != null) process.destroy();
        }
    }


    private boolean testRootAccess() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("id\n");
            os.writeBytes("exit\n");
            os.flush();

            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            return false;
        } finally {
            closeQuietly(os);
            if (process != null) process.destroy();
        }
    }

    private void updateStatus(String text) {
        runOnUiThread(() -> tvStatus.setText(text));
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void closeQuietly(DataOutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (Exception ignored) {
            }
        }
    }
}