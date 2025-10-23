# 🚀 Firebase Cloud Messaging (FCM) Service with Spring Boot

Dự án này là một giải pháp hoàn chỉnh triển khai dịch vụ Gửi thông báo đẩy (Push Notification) sử dụng **Spring Boot Backend** và **Firebase Cloud Messaging (FCM)**, cùng với việc tích hợp thành công trên ứng dụng **Android**.

## ✨ Thành quả dự án

Hai hình ảnh dưới đây là minh chứng trực quan cho sự hoạt động thành công của toàn bộ hệ thống.

### 1. Thông báo đẩy trên Android

Hình ảnh xác nhận thông báo được gửi từ API backend đã hiển thị chính xác trên thiết bị di động, bao gồm cả nội dung và hình ảnh.

| Mô tả | Chi tiết |
| :--- | :--- |
| **Tiêu đề** | "Thông báo Mới từ Server" |
| **Nội dung** | "Nội dung chi tiết của thông báo sẽ hiển thị trên thiết bị." |
| **Hình ảnh** | Thông báo kèm theo hình ảnh minh họa (sử dụng trường `image` trong payload). |

<img width="572" height="925" alt="Android" src="https://github.com/user-attachments/assets/0f4a0730-4e64-47d9-82ac-0eed072894c7" />

### 2. Dashboard Báo cáo Chiến dịch Firebase

Hình ảnh từ Firebase Console cho thấy các thông báo đã được gửi thành công và ghi nhận trạng thái **Completed** trong hệ thống theo dõi của Firebase Messaging.

| Mô tả | Chi tiết |
| :--- | :--- |
| **Trạng thái** | `Completed` |
| **Chỉ số** | Ghi nhận số lượng gửi (`Sends / Impressions: <1000`). |

<img width="1347" height="717" alt="Screenshot 2025-10-23 141047" src="https://github.com/user-attachments/assets/9ab58c79-ebfc-4622-83b7-73d11f6d1ddd" />

---

## 🛠️ Công nghệ sử dụng

| Lĩnh vực | Công nghệ | Chi tiết |
| :--- | :--- | :--- |
| **Backend** | Spring Boot | Dùng cho RESTful API |
| **Messaging** | Firebase Admin SDK | Phiên bản 9.2.0 |
| **Build Tool** | Maven | |
| **Ngôn ngữ** | Java 8 | |

---

## 🌐 API Endpoints

Dịch vụ cung cấp hai endpoint chính để gửi thông báo (Cổng mặc định: `9090`).

| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `http://localhost:9090/notification` | Gửi thông báo tới một **Device Token** cụ thể. |
| `POST` | `http://localhost:9090/notification/topic` | Gửi thông báo tới tất cả thiết bị đã **Subscribe** vào **Topic**. |

### Cấu trúc Payload (`NotificationMessage`)

```json
{
  "recipientToken": "DEVICE_TOKEN_HOẶC_TÊN_TOPIC",
  "title": "Tiêu đề thông báo",
  "body": "Nội dung chi tiết",
  "image": "URL_HÌNH_ẢNH_TÙY_CHỌN",
  "data": {
    "key1": "value1",
    "deeplink": "/app/settings"
  }
}
