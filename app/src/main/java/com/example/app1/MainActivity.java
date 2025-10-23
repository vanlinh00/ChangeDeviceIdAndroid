package com.example.app1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.DataOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_REAL_DEVICE_ID = "real_device_id";

    private static final int ANDROID_ID_LENGTH = 16;

    private TextView tvStatus;
    private Button btnCheckPermissionID, btnChangeSystemAndroidID, btnRealDeviceID, btnReSaveRealDeviceID,btnGetFCMToken;
    private SharedPreferences sharedPreferences;
    private ImageButton btnCopyToken; // <--- Thêm khai báo này

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
        btnGetFCMToken.setOnClickListener(view -> getAndDisplayFCMToken());

        // Khởi tạo Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        logFirebaseConfigEvent();
        Log.d("FirebaseCheck", "Firebase Analytics object initialized."); // Ghi log kiểm tra

        setupCopyTokenListener(); // <--- GỌI HÀM SETUP
    }
    private void setupCopyTokenListener() {
        btnCopyToken.setOnClickListener(view -> {
            String statusText = tvStatus.getText().toString();
            if (statusText.startsWith("FCM Token: ")) {
                // Lấy phần token bằng cách loại bỏ prefix "FCM Token: "
                String token = statusText.substring("FCM Token: ".length());
                copyToClipboard("FCM Token", token);
            } else {
                showToast("Không có FCM Token để sao chép.");
            }
        });
    }
    private void copyToClipboard(String label, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            showToast("Đã sao chép " + label + " vào clipboard!");
        }
    }
    private void getAndDisplayFCMToken() {
        // Hàm này sẽ lấy Token và hiển thị lên TextView
        updateStatus("Đang lấy FCM Token...");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String errorMsg = "Lấy FCM Token thất bại: " + task.getException().getMessage();
                        Log.w("FCM_TOKEN_CHECK", errorMsg, task.getException());
                        updateStatus(errorMsg);
                        showToast("Lấy FCM Token thất bại");
                        return;
                    }
                    // Lấy Token và hiển thị lên giao diện
                    String token = task.getResult();
                    Log.d("FCM_TOKEN_CHECK", "FCM Registration Token: " + token);
                    String statusText = "FCM Token: " + token;
                    updateStatus(statusText);
                    showToast("Đã lấy được FCM Token.");

                    btnCopyToken.setVisibility(android.view.View.VISIBLE);
                });
    }

    private void logFirebaseConfigEvent() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "initial_config_check_java");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "config");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        btnCheckPermissionID = findViewById(R.id.btnCheckPermissionID);
        btnChangeSystemAndroidID = findViewById(R.id.btnChangeSystemID);
        btnRealDeviceID = findViewById(R.id.btnRealDeviceID);
        btnReSaveRealDeviceID = findViewById(R.id.btnReSaveRealDeviceID);
        btnGetFCMToken = findViewById(R.id.btnGetFCMToken);
        btnCopyToken = findViewById(R.id.btnCopyToken);

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
        runOnUiThread(() -> {
            tvStatus.setText(text);
            // Ẩn nút copy nếu status không phải là FCM Token
            if (!text.startsWith("FCM Token: ")) {
                btnCopyToken.setVisibility(android.view.View.GONE);
            }
        });    }

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