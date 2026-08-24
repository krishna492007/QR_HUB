# QR HUB — Ultra-Fast QR & Barcode Suite

<p align="center">
  <img src="QR_HUB/app/src/main/ic_launcher-playstore.png" width="100" height="100" alt="QR HUB Icon" style="border-radius: 20px;">
</p>

<p align="center">
  <b>A modern, privacy-first, lightning-fast QR code scanner, UPI auto-payer, and multi-format offline QR generator for Android.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg" alt="Compose">
  <img src="https://img.shields.io/badge/ML%20Kit-Barcode%20Scanning-orange.svg" alt="ML Kit">
  <img src="https://img.shields.io/badge/Target%20SDK-35-brightgreen.svg" alt="Target SDK">
</p>

---

## ✨ Features

### 🔍 1. Ultra-Fast Scanner
- **Google ML Kit Vision:** On-device millisecond optical barcode detection.
- **CameraX Engine:** Optimized live preview with flash torch and gallery image import.
- **Cyberpunk Viewfinder:** Smooth amber corner brackets with sweeping cyan scanning laser beam.
- **Vibration & Audio Feedback:** Instant haptic response upon successful scan.

### ⚡ 2. Smart UPI Payment Routing
- **Direct UPI Parsing:** Automatically parses Payee VPA, Payee Name, Amount, and Note.
- **Default App Preference:** Set your favorite UPI app (PhonePe, Google Pay, Paytm, BHIM, Navi, CRED, etc.) or choose per scan.
- **Quick Auto-Pay Mode:** Launch your preferred payment app instantly upon scanning without extra clicks.

### 🎨 3. Multi-Format QR Generator (Offline)
Generate high-contrast, instant-scanning QR codes for 11+ formats:
- **Text & Notes**
- **Website URLs**
- **UPI Payments** (Custom VPA, Amount & Note)
- **Phone Numbers & Direct Calls**
- **SMS Messages**
- **Email Drafts**
- **WiFi Network Configuration** (Auto-connect string)
- **vCard Contacts** (Name, phone, email, organization, title)
- **WhatsApp Direct Chat & Group Links**
- **Geo Locations / GPS Coordinates**
- **vCalendar Event Invites**

### 📜 4. Complete Local History & Favorites
- **Offline Room Database:** All scanned and generated items are stored locally.
- **Category Filter Chips:** Filter by `All`, `Scanned`, `Created`, and `Favorites`.
- **Search & Filter:** Instant real-time text query search across all history.
- **Favorite System:** Quick bookmarking with spring pop bounce animation.
- **Export & Share:** Export history as CSV/TXT or save high-resolution QR bitmaps directly to device storage.

### 🛡️ 5. Privacy-First Architecture
- **100% Offline & Private:** No camera feeds, photos, or scan histories are uploaded to any external server.
- **No Intrusive Permissions:** Restricted permissions removed for full Google Play Store compliance.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose (Material Design 3 with custom Dark & Amber Theme)
- **Camera Engine:** AndroidX CameraX (Preview, ImageAnalysis)
- **Machine Learning:** Google ML Kit Barcode Scanning (On-Device)
- **QR Engine:** ZXing (`core:3.5.3`) with custom pixel-perfect B&W rendering
- **Database:** AndroidX Room Database with Kotlin Coroutines & Flow
- **Image Loading:** Coil Compose
- **Architecture:** Single Activity, Declarative UI, MVVM Pattern with StateFlow

---

## 📁 Project Structure

```
QR_HUB/
├── app/
│   ├── src/main/
│   │   ├── java/com/qr/hub/
│   │   │   ├── data/             # Room Database, DAO, and Entities
│   │   │   ├── generate/         # QR Generator UI & Generator Engine
│   │   │   ├── history/          # History List, Segmented Tabs & Detail Screen
│   │   │   ├── privacy/          # In-app Privacy Policy Screen
│   │   │   ├── scanner/          # CameraX, Scanner Viewfinder & Result Composables
│   │   │   ├── ui/theme/         # Color Palette, Typography & Compose Themes
│   │   │   ├── util/             # Helpers, UPI Preference Manager & Theme Tokens
│   │   │   ├── viewmodel/        # History ViewModel & State Management
│   │   │   └── MainActivity.kt   # App Entry Point & Navigation Orchestration
│   │   ├── res/                  # Drawables, Mipmaps, and XML configs
│   │   └── AndroidManifest.xml   # App permissions and query declarations
│   ├── build.gradle.kts          # App dependencies & packaging configs
│   └── proguard-rules.pro        # Proguard / R8 optimization rules
├── privacy_policy.html           # Standalone Privacy Policy for Play Console
└── build.gradle.kts              # Root build configuration
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 (Adoptium / Eclipse Temurin)
- Android SDK (API 35)

### Build & Run
```bash
# Clone the repository
git clone https://github.com/krishna492007/QR_HUB.git
cd QR_HUB/QR_HUB

# Build and install on connected device
./gradlew installDebug
```

### Build Release Bundle (for Google Play Store)
```bash
./gradlew bundleRelease
```
The output `.aab` will be generated at:  
`app/build/outputs/bundle/release/app-release.aab`

---

## 📄 Privacy Policy
The official Privacy Policy for **QR HUB** is available at [`privacy_policy.html`](QR_HUB/privacy_policy.html).

---

## 👨‍💻 Author
**Built by Krishna**  
- **GitHub:** [@krishna492007](https://github.com/krishna492007)  
- **Email:** support.qrhub@gmail.com

---

<p align="center">
  <sub>© 2026 QR HUB by Krishna. All rights reserved.</sub>
</p>
