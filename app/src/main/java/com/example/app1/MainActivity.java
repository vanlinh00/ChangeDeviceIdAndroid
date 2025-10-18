package com.example.app1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView  tvStatus;
    private Button btnbtnCheckPermissionID, btnChangeSystemAndroidID, btnRealDeviceID;
    private Button btnReSaveRealDeviceID;

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_REAL_DEVICE_ID = "real_device_id";

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnbtnCheckPermissionID = findViewById(R.id.btnCheckPermissionID);
        btnChangeSystemAndroidID = findViewById(R.id.btnChangeSystemID);

        btnRealDeviceID = findViewById(R.id.btnRealDeviceID);
        btnReSaveRealDeviceID = findViewById(R.id.btnReSaveRealDeviceID);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!sharedPreferences.contains(KEY_REAL_DEVICE_ID)) {
            String realDeviceId = getRealAndroidID();
            sharedPreferences.edit().putString(KEY_REAL_DEVICE_ID, realDeviceId).apply();
            tvStatus.setText("Lần đầu lưu Real Android ID: " + realDeviceId);
        } else {
            String savedId = sharedPreferences.getString(KEY_REAL_DEVICE_ID, "");
            tvStatus.setText("Real Android ID đã lưu: " + savedId);
        }



        btnbtnCheckPermissionID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestRootPermission();
            }
        });

        btnChangeSystemAndroidID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSystemAndroidID();
            }
        });


        btnRealDeviceID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String realDeviceID = getRealAndroidID();
                tvStatus.setText("Real Android ID: " + realDeviceID);
            }
        });

        btnReSaveRealDeviceID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String savedAndroidID = sharedPreferences.getString(KEY_REAL_DEVICE_ID, "Chưa có Android ID nào được lưu");
                tvStatus.setText("Android ID thực đã lưu: " + savedAndroidID);
                Toast.makeText(MainActivity.this, "Đã hiển thị Real Android ID đã lưu.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String generateNewDeviceID() {
        return UUID.randomUUID().toString();
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
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void changeSystemAndroidID() {
        String newAndroidID = generateNewDeviceID().replace("-", "").substring(0, 16);
        // Android ID thường 16 ký tự hexa, điều chỉnh cho hợp lý

        String command = "settings put secure android_id " + newAndroidID;

        boolean success = runRootCommand(command);

        if (success) {
            tvStatus.setText("Đã đổi Android ID hệ thống thành: " + newAndroidID);
            Toast.makeText(this, "Thay đổi Android ID thành công", Toast.LENGTH_LONG).show();
        } else {
            tvStatus.setText("Thay đổi Android ID thất bại, kiểm tra quyền root");
            Toast.makeText(this, "Không thể thay đổi Android ID, app cần quyền root", Toast.LENGTH_LONG).show();
        }
    }

    // Lấy Android ID thật hiện tại để kiểm tra
    private String getRealAndroidID() {
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    private void checkRoot() {
        if (isDeviceRooted()) {
            Toast.makeText(this, "Thiết bị đã cấp quyền root cho app", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Thiết bị chưa cấp quyền root cho app", Toast.LENGTH_LONG).show();
        }
    }
    public boolean isDeviceRooted() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            return (exitValue == 0);
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
    private void requestRootPermission() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            // Gửi lệnh đơn giản để kích hoạt quyền root
            os.writeBytes("id\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Toast.makeText(this, "Đã có quyền ROOT!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không có quyền ROOT hoặc bị từ chối.", Toast.LENGTH_SHORT).show();
            }
            os.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi yêu cầu quyền ROOT.", Toast.LENGTH_SHORT).show();
        }
    }




}