# 📱 QR HUB — Complete Feature Documentation & Architecture Guide

> **QR HUB** is a flagship, privacy-first, dark-luxury QR Code & Barcode ecosystem designed for Android. It combines lightning-fast camera scanning, 100% fail-proof UPI payments, creative custom branding, bulk CSV batch generation, and commercial A4 sticker sheet printing into a single ultra-lightweight (12.95 MB) package.

---

## 🌟 Master Feature Matrix

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                                  QR HUB                                      │
├──────────────────────────────────────────────────────────────────────────────┤
│  ⚡ Zero-Delay ML Scanner    │  💸 1-Tap UPI Payment Engine (Zero Failure)   │
│  🎨 Hex Brand Color Studio   │  📦 Bulk QR Generator (CSV -> ZIP & PDF)      │
│  🏷️ A4 Barcode Sticker Sheet │  🔒 URL Safe-Browsing & Link Security Shield  │
│  📶 1-Tap WiFi Auto-Connect  │  📜 Searchable History & Offline Gallery      │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Detailed Feature Breakdown

### 1. ⚡ Real-Time ML Scanner Engine
- **CameraX + Google ML Kit:** Sub-50ms barcode and QR detection from camera stream and photo gallery.
- **Smart Zoom & Auto-Torch:** Pinch-to-zoom, tap-to-focus, and integrated flashlight toggle for low-light scanning.
- **Multi-Format Support:** Reads standard 2D formats (QR Code, Data Matrix, Aztec, PDF417) and 1D Product Barcodes (EAN-13, EAN-8, UPC-A, UPC-E, Code-128, Code-39, Code-93, ITF).

### 2. 💸 100% Reliable UPI Payment Engine
- **Direct App Intent Launch:** Bypasses Android 11+ intent chooser bugs with direct package launching (`PhonePe`, `Google Pay`, `Paytm`, `BHIM`, `Cred`, `Navi`, `Amazon Pay`, etc.).
- **Automatic Gallery QR Backup:** Every scanned UPI QR is automatically saved to the device gallery in high resolution so users can scan from within banking apps if external intent routing fails.
- **Custom Amount Pre-Fill:** Allows on-the-fly bill amount adjustments before launching payment.

### 3. 🎨 Dark Luxury QR Design Studio & Hex Brand Colors
- **12 Individual QR Types:** Text, Website URL, UPI, Phone Call, WhatsApp, WiFi, SMS, Email, Contact Card (vCard), Calendar Event, Maps / Geo Location, and Plus Code.
- **Custom Brand Color Picker:**
  - Full `#RRGGBB` Hex Code input with real-time validation.
  - Granular target switcher: apply colors to **Dots / Matrix**, **Linear Gradient (Start/End)**, or **Background**.
  - 12 curated quick-pick palettes (Cyber Amber, Neon Cyan, Emerald Mint, Royal Purple, Rose Gold, Midnight Obsidian, etc.).
- **Visual Styling:** Multiple dot styles (Square, Rounded, Circle, Diamond, Sleek), corner eye shapes, embedded center icons/logos, and high-resolution transparent PNG export.

### 4. 📦 Bulk / Batch QR Code Generator
- **Multi-Line Text & CSV Import:** Paste 100+ lines or import `.csv`/`.txt` files with 1-click parsing.
- **Live Generation Progress:** Background coroutine-driven rendering with real-time percentage progress indicators.
- **Export Options:**
  - **ZIP Archive:** All high-resolution PNG QR images named and packed into a single zip file.
  - **Printable A4 PDF Sheet:** Neatly arranged grid of QR codes with custom labels and page headers ready for printing.

### 5. 🏷️ Product Barcode Studio & Multi-Page A4 Sticker Sheets
- **1D Barcode Standards:** Full support for `Code-128`, `EAN-13` (13 digits), `UPC-A` (12 digits), and `Code-39` with crisp human-readable text printed below stripes.
- **4 Commercial Pre-Cut Sticker Formats:**
  1. **`⭐ 24 Labels (3x8)`** — Standard retail, supermarket & grocery products (`64mm x 34mm`).
  2. **`📦 12 Labels (2x6)`** — Carton boxes, shipping parcels, and logistics tags (`99mm x 38mm`).
  3. **`💊 40 Labels (4x10)`** — Chote items, medicines, lipstick, stationery & cosmetics (`48mm x 25mm`).
  4. **`🏷️ 65 Labels (5x13)`** — Jewellery tags, micro-electronics & components (`38mm x 21mm`).
- **Multi-Page Sticker Printing Engine:**
  - Interactive stepper `[-] [Qty] [+]` and quick count chips (`24`, `48`, `100`, `200` pcs).
  - Real-time A4 page calculator (e.g. `105 Stickers = 5 A4 Pages`).
  - True multi-page PDF generation with exact grid margins and cut-out guides.
- **Bulk CSV Barcode Generator:** Import product catalogs and generate 100+ unique barcodes in a multi-page PDF sheet or ZIP archive.

### 6. 🔒 URL Safe-Browsing & Link Security Inspector
- **Dynamic Trust Shield:** Evaluates scanned URLs in real time and classifies them as **SAFE**, **CAUTION**, or **SUSPICIOUS**.
- **HTTPS SSL Validation:** Displays green lock badge for encrypted connections and alerts on insecure plaintext HTTP.
- **Shortened URL Expander:** Detects shorteners (`bit.ly`, `tinyurl.com`, `t.co`, `cutt.ly`, `is.gd`, etc.) and unmasks the redirect chain in the background to reveal the real destination domain *before* opening.
- **Threat Heuristics:** Detects raw IP addresses, suspicious lookalikes, and direct executable downloads (`.apk`, `.exe`, `.bat`).

### 7. 📶 1-Tap WiFi Auto-Connect (Android 10 - 16)
- **Native System Sheet Prompt:** Uses `WifiNetworkSuggestion` and `WifiNetworkSpecifier` APIs to display Android's official *"Connect to Wi-Fi network?"* prompt.
- **Zero-Typing Experience:** Instantly joins WPA2, WPA3, and Open networks without manual password copy-pasting.
- **Legacy Fallback:** Automatically copies password to clipboard as a fail-safe.

### 8. 📜 History, Search & Monetization
- **Local Room Database:** 100% offline history storage with fast search, category filtering, and favorite pinning.
- **Re-Styling & Sharing:** Open any past scan to re-style its QR code, change colors, or re-export.
- **Zero-Delay Monetization:** Optimized Start.io Banner & Interstitial integration with capped frequency so user experience remains fluid.

---

## 🛠️ Technical Specifications

| Parameter | Specification |
| :--- | :--- |
| **Language** | Kotlin 1.9.22 |
| **UI Framework** | Jetpack Compose (Material 3 Dark Luxury Design) |
| **Minimum SDK** | Android 7.0 (API Level 24) |
| **Target SDK** | Android 14 / 15 / 16 (API Level 34-36) |
| **Camera & ML** | CameraX 1.3.4 + Google ML Kit Barcode Scanning 17.3.0 |
| **Barcode & QR Generation** | ZXing Core 3.5.3 (Matrix + 1D Writers) |
| **PDF Generation** | Native Android `android.graphics.pdf.PdfDocument` |
| **APK Size** | **12.95 MB** (Post R8 Minification & Resource Shrinking) |

---

## 🔑 Release Signing & Keystore Details

- **Keystore Path:** `QR_HUB/app/qrhub_release.jks`
- **Key Alias:** `qrhub`
- **Password:** `qrhub12345`
- **Release APK Output:** `QR_HUB_v1.0_Signed.apk`

---

## 📂 Source Directory Structure

```
com.qr.hub/
├── data/                  # Room Database, DAOs & Entities
├── generate/              # QR Generation Screens, Styling Engine, Batch & Barcode Studio
│   ├── BatchQrGeneratorScreen.kt
│   ├── CustomColorPickerDialog.kt
│   ├── GenerateBarcodeScreen.kt
│   ├── GenerateQrScreens.kt
│   ├── QRGenerator.kt
│   └── QRStylingComponents.kt
├── history/               # History List & History Detail Screens
│   ├── HistoryDetailScreen.kt
│   └── HistoryScreen.kt
├── model/                 # Sealed Classes & Data Models (ScannedQR, QRType)
├── scanner/               # CameraX Preview, Scanner Overlay & Result Composable
│   ├── CameraXPreview.kt
│   ├── ResultScreenComposable.kt
│   └── ScannerScreen.kt
├── ui/theme/              # Dark Luxury Amber/Cyan Theme Palette & Typography
└── util/                  # Utilities, Ad Manager, Security Inspector & WiFi Connector
    ├── QRDetector.kt
    ├── WifiAutoConnector.kt
    └── security/
        ├── UrlSecurityInspector.kt
        └── UrlSecurityCard.kt
```

---

*© 2026 QR HUB. Built with Jetpack Compose & Google ML Kit.*
