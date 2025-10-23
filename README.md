# 🚀 Project Name - Spring Boot Backend

Dự án này là backend phục vụ cho ứng dụng di động, được thiết kế với khả năng mở rộng.

## 1. Firebase Cloud Messaging (FCM) Service

Phần này triển khai dịch vụ Gửi thông báo đẩy (Push Notifications) sử dụng **Spring Boot** và **Firebase Admin SDK**.

### Thành quả FCM

Hệ thống đã hoạt động thành công, chứng minh bằng hai kết quả sau:

1.  **Thông báo Android:** Thông báo đẩy được gửi từ backend đã hiển thị chính xác trên thiết bị di động.
    <img width="572" height="925" alt="Android" src="https://github.com/user-attachments/assets/0f4a0730-4e64-47d9-82ac-0eed072894c7" />

2.  **Báo cáo Firebase Console:** Ghi nhận thông báo đã được gửi thành công.
    <img width="1347" height="717" alt="Screenshot 2025-10-23 141047" src="https://github.com/user-attachments/assets/9ab58c79-ebfc-4622-83b7-73d11f6d1ddd" />

### API Endpoints

| Phương thức | Endpoint | Chức năng |
| :--- | :--- | :--- |
| `POST` | `/notification` | Gửi thông báo tới **Device Token** cụ thể. |
| `POST` | `/notification/topic` | Gửi thông báo tới **Topic** đã đăng ký. |

---

## 2. [Phần mở rộng khác]

_**(Ví dụ: User Authentication, Data Persistence)**_

## 3. [Phần mở rộng khác]

---

## 🛠️ Công nghệ

* **Backend:** Spring Boot, Java 8
* **Messaging:** Firebase Admin SDK
* **Build:** Maven
