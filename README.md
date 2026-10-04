# CanvasShadow

Thư viện Android vẽ shadow bằng `Canvas`, hỗ trợ màu theo state, cấu hình XML và thay đổi runtime. Shadow được cache; đổi màu/state không tạo lại bitmap blur.

## Cài từ JitPack

Đổi `your-github-username` trong `gradle.properties`, push repository lên GitHub và tạo tag (ví dụ `1.0.0`). Thêm JitPack vào project sử dụng:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

```kotlin
dependencies {
    implementation("com.github.YOUR_GITHUB_USERNAME:YOUR_REPOSITORY:1.0.0")
}
```

## Dùng trong XML

```xml
<quylh.canvas.shadow.ShadowLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    app:shadowColor="#66000000"
    app:shadowAlpha="0.85"
    app:shadowBlur="16dp"
    app:shadowOffsetX="0dp"
    app:shadowOffsetY="6dp"
    app:shadowCornerRadius="20dp"
    app:shadowSpread="1dp"
    app:shadowMode="outer">

    <!-- Nội dung -->
</quylh.canvas.shadow.ShadowLayout>
```

`shadowColor` nhận được `ColorStateList`, nên có thể trỏ tới selector trong `res/color`.

`shadowMode` có ba giá trị: `outer` (từ trong ra ngoài), `inner` (từ ngoài vào trong) và `both` (cả hai).

## Thay đổi runtime

```kotlin
shadowLayout.shadowConfig = shadowLayout.shadowConfig.copy(
    blur = shadowLayout.dp(24f),
    offsetY = shadowLayout.dp(8f),
    alpha = 0.7f,
    mode = ShadowMode.BOTH,
)
shadowLayout.setShadowColor(Color.RED)
```

`ShadowLayout` tự cộng khoảng trống cần thiết vào padding để shadow không bị cắt; padding khai báo bởi app vẫn được giữ cho nội dung.

## Publish

```bash
git tag 1.0.0
git push origin 1.0.0
```

Mở `https://jitpack.io/#YOUR_GITHUB_USERNAME/YOUR_REPOSITORY/1.0.0` và bấm **Get it**.
