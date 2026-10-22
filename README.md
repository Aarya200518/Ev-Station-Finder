# EV Station Finder - Native Android App

A native Android application built using **Kotlin**, **Jetpack Compose**, **Room Database**, **Material 3**, and **Coroutines / StateFlow**.

---

## 📱 Features

- **Nearby EV Station Explorer**: Filter by connector types (CCS, Type 2, Fast 50kW+), rating, distance, price/kWh, and available slots.
- **Slot Reservation & Battery Calculator**: Select vehicle, adjust target battery sliders to estimate required energy (kWh) and charging duration (~mins). Select date & time slot.
- **Payment & Review**: Review fare breakdown (Energy cost, Platform fee, GST tax) and select payment method (UPI, Card, NetBanking, EV Wallet).
- **Instant Slot Booking & Slot Management**: Real-time slot availability updates backed by local Room Database.
- **My Reservations**: View active/upcoming bookings with cancellation option (releases slot) and past booking history.
- **Driver Profile & Vehicle Manager**: Manage driver info, saved locations, and EV vehicle specs.
- **Interactive Map View**: Interactive station pins on map with distance indicators and quick booking cards.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Material 3 Design System)
- **Database & Persistence**: Room Database (`AppDatabase`, `StationDao`, `VehicleDao`, `BookingDao`, `ProfileDao`)
- **Architecture**: MVVM + Repository Pattern (`AppViewModel`, `AppRepository`)
- **Navigation**: Jetpack Compose Navigation & StateFlow screen transitions
- **Build System**: Gradle 8.11 (`build.gradle.kts` with KSP plugin)

---

## 🚀 How to Run in Android Studio

1. Open **Android Studio**.
2. Select **Open** and choose this project folder: `/Users/aaryabhoye/Desktop/EV-Station-Finder 2`.
3. Allow Android Studio to sync Gradle dependencies automatically.
4. Select an **Android Emulator** or connect a physical Android device.
5. Click **Run (`Shift + F10` / Play Button)**!

---

## 📂 Project Structure

```
EV-Station-Finder 2/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       └── java/com/evstationfinder/
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── Models.kt
│           │   ├── AppDatabase.kt
│           │   └── AppRepository.kt
│           └── ui/
│               ├── Theme.kt
│               ├── AppViewModel.kt
│               ├── HomeScreen.kt
│               ├── BookSlotScreen.kt
│               ├── PaymentScreen.kt
│               ├── ConfirmationScreen.kt
│               ├── BookingsScreen.kt
│               ├── ProfileScreen.kt
│               └── MapScreen.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```
