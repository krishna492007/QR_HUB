# Dukan Bill Manager — Documentation Master Index

Welcome to the technical documentation repository for **Dukan Bill Manager** — an enterprise-grade, mobile-first business billing, Indian GST tracking, supplier ledger (Khata), inventory rate intelligence, and automated AI invoice extraction system.

---

## 📚 Documentation Index

| File | Document Title | Primary Focus & Target Audience |
| :--- | :--- | :--- |
| [`01_PRD.md`](01_PRD.md) | **Product Requirement Document (PRD)** | User Personas, Core Features, Functional & Non-Functional Requirements, Business Rules. |
| [`02_UI_UX_SPEC.md`](02_UI_UX_SPEC.md) | **UI/UX Specification & Design System** | Mobile-First Bottom Navigation, Gestures, Bottom Sheets, Stitch Design Tokens, B&W Thermal Print. |
| [`03_SYSTEM_ARCHITECTURE.md`](03_SYSTEM_ARCHITECTURE.md) | **System Architecture Document** | Android Clean Architecture (Compose + Room + Retrofit), Express Backend, Multi-Key OCR Failover Engine. |
| [`04_DATABASE_SCHEMA.md`](04_DATABASE_SCHEMA.md) | **Database Schema & Data Models** | Room SQLite Entities, DAOs, TypeConverters, Server Multi-Tenant JSON State Schema, Indexed Queries. |
| [`05_API_SPECIFICATION.md`](05_API_SPECIFICATION.md) | **REST API Specification** | JWT Auth Contracts (Cookie/Bearer), State Sync, Gemini OCR Endpoint, Receipt Uploads, Admin Routes. |
| [`06_IMPLEMENTATION_ROADMAP.md`](06_IMPLEMENTATION_ROADMAP.md) | **Implementation Roadmap & QA Strategy** | 9-Sprint Android Native Execution Plan, Milestone Deliverables, Verification Checklist & Release APK. |
| [`07_IMPORT_SPECIFICATION_AND_FORMATS.md`](07_IMPORT_SPECIFICATION_AND_FORMATS.md) | **Invoice & Bill Import Specification** | Universal CSV Auto-Mapper, Column Synonyms Dictionary, Gemini AI OCR Pipeline, Digital PDF.js Extraction. |

---

## 🏛️ System Overview & Dual-Engine Model

```mermaid
graph TB
    subgraph Client Layer
        MobileApp["Native Android App<br/>(Kotlin + Jetpack Compose + Material 3)"]
        WebApp["Web SaaS Client<br/>(Pure Vanilla HTML5/CSS3/ES6+ SPA)"]
    end

    subgraph Local Storage & Cache
        RoomDB[("Room SQLite DB<br/>(Bills, Suppliers, Payments, Items, SyncQueue)")]
        LocalCache[("Web LocalStorage<br/>(State Snapshot & Mapping Cache)")]
    end

    subgraph Backend API (Node.js & Express)
        Gateway["Express Gateway (:3000)<br/>(JWT Auth & Rate Limiters)"]
        OCRService["AI OCR Proxy<br/>(5-Key Gemini Failover Pool)"]
        FileStore[("Atomic JSON Store<br/>(users/<userId>.json)")]
        UploadStore[("Receipts Storage<br/>(uploads/receipts/*.jpg)")]
    end

    MobileApp <--> RoomDB
    WebApp <--> LocalCache
    MobileApp <-->|"HTTPS / REST (Bearer Auth)"| Gateway
    WebApp <-->|"HTTPS / REST (HttpOnly Cookie)"| Gateway
    Gateway --> OCRService
    Gateway --> FileStore
    Gateway --> UploadStore
```

---

## 🚀 Quick Start for App Developers

### Android Native Environment Setup
* **IDE:** Android Studio Ladybug (2024.2+) or newer.
* **JDK:** OpenJDK 17 or 21.
* **Language & UI Toolkit:** Kotlin 2.0+ with Jetpack Compose & Material 3.
* **Target SDK:** 35 (Android 15), Min SDK: 26 (Android 8.0 Oreo).
* **Architecture:** MVVM + Clean Architecture (UI -> ViewModel -> UseCase -> Repository -> Room / Retrofit).

### Backend Server Setup
* **Runtime:** Node.js 18+ LTS.
* **Start Server:** Run `start-server.bat` in `update app/` directory.
* **LAN Testing:** Server automatically binds to `0.0.0.0:3000` and displays terminal ANSI QR codes for camera scan from physical Android devices.
