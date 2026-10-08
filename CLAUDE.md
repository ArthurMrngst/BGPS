# BGps — Project Instructions

## Overview
BGps is a graduation project: an Android app that automatically records daily
spending and travel activity by combining (1) bank/e-wallet notification
parsing, (2) receipt OCR, and (3) GPS-based trip detection into one
categorised spending/lifestyle report.

## Tech stack
- Language: Kotlin (not Java)
- UI: Jetpack Compose (not XML Views)
- Architecture: MVVM
- DI: Hilt
- Local DB: Room
- Networking: Retrofit
- Async: Kotlin Coroutines + Flow
- Maps: Google Maps SDK for Android
- Backend (separate repo/service): NestJS (TypeScript) + TypeORM/Prisma + PostgreSQL, JWT auth, Swagger docs

## Package structure
- `data/` — repositories, Room database (entities/DAOs), network models/DTOs
- `domain/` — business logic, use cases (no Android framework dependencies)
- `ui/` — Composable screens + ViewModels
- `di/` — Hilt modules

## Key architectural decisions — do not relitigate without discussion
- **Native Android only** (no Flutter/React Native). The core data-capture
  feature is Android-only regardless of framework, so a cross-platform layer
  would only add complexity, not remove it.
- **Financial data source is `NotificationListenerService`, NOT SMS reading.**
  This user's banks/e-wallets only send app notifications, not real SMS.
  Always whitelist-filter by sender package name inside
  `onNotificationPosted()` before parsing or storing anything — never process
  notifications from unrecognised apps.
- **Cash transactions with no printed receipt** fall back to manual
  quick-entry (no OCR possible for those).
- **Privacy constraint:** raw notification text / SMS text must never leave
  the device. Only structured, already-parsed transaction data (amount, type,
  merchant, timestamp) may be sent to the backend or stored beyond the
  parsing step.

## Conventions
- `minSdk` 26; `compileSdk`/`targetSdk` = latest stable at time of setup.
- Keep per-source parsing patterns (regex per bank/wallet app) together in
  one place under `data/parsing/`, not scattered across the codebase.
- Every new automated data-capture module (notification, OCR, GPS) should be
  testable independently of the UI layer.

## Build & run
<!-- Fill in once the project is created, e.g.:
./gradlew assembleDebug
./gradlew test
-->

## Useful context
- Full project proposal (Objectives, WBS, Gantt): see BGps_Project_Proposal.docx
- Report template (final deliverable structure): see FinalReportTemplate.docx
