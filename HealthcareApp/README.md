# Healthcare Portal — Android App

## Overview

Native Android app (Kotlin, Material Design 3) for the Healthcare Portal system.

### Screens
- **Home** — Hero banner + department grid (with images)
- **Doctors** — Doctor cards with photo, specialty, availability badge, consultation fee
- **Book Appointment** — 3-step flow: Doctor → Patient Info → Payment
- **Payment** — EVC Plus Hormuud merchant flow (USSD push) + manual payment
- **Track Appointment** — Real-time status with progress bar
- **My Appointments** — List of bookings with status
- **Profile** — Login/logout

---

## Setup Instructions

### 1. Open in Android Studio
1. Open Android Studio (Hedgehog or newer)
2. **File → Open** → select the `HealthcareApp` folder
3. Wait for Gradle sync to complete

### 2. Configure Server URL

Edit `app/build.gradle`:
```groovy
buildTypes {
    debug {
        // Your PC's local IP (not localhost — emulator/phone can't reach localhost)
        buildConfigField "String", "BASE_URL", '"http://192.168.1.X:PORT/"'
    }
    release {
        buildConfigField "String", "BASE_URL", '"https://appointment.organization.com/"'
    }
}
```

**Finding your PC IP on Windows:**
```
ipconfig
```
Look for `IPv4 Address` under your network adapter (e.g., `192.168.1.105`).

**Finding the port:**
In Visual Studio, check the Properties/launchSettings.json or the URL shown when the app starts (e.g., `https://localhost:56359` → use port `56359`).

### 3. Backend API - Required Endpoints

The app calls these endpoints on your ASP.NET backend:

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/departments` | List active departments |
| GET | `/Appointment/GetDoctors?departmentId=X` | Doctors by department |
| GET | `/api/payment-methods` | Payment methods |
| POST | `/api/appointments` | Book appointment |
| POST | `/Home/TrackResult` | Track by number or mobile |
| GET | `/api/appointments` | My appointments (auth) |
| POST | `/api/payment/hormuud/initiate` | EVC Plus payment |
| GET | `/api/payment/hormuud/status/{ref}` | EVC status poll |

> **Note:** Some of these endpoints (`/api/departments`, `/api/payment-methods`) may need to be added to your backend. The `/Appointment/GetDoctors` and `/Home/TrackResult` already exist.

### 4. Add Missing API Endpoints to Backend

Add this to your `AppointmentsApiController.cs`:

```csharp
// GET: /api/departments
[HttpGet("/api/departments")]
public async Task<IActionResult> GetDepartments()
{
    var depts = await _uow.Departments.FindAsync(d => d.OrganizationId == 1 && d.IsActive);
    return Ok(ApiResponse<object>.Ok(depts.OrderBy(d => d.DisplayOrder)));
}

// GET: /api/payment-methods
[HttpGet("/api/payment-methods")]
public async Task<IActionResult> GetPaymentMethods()
{
    var methods = await _uow.PaymentMethods.FindAsync(pm => pm.OrganizationId == 1 && pm.IsActive);
    return Ok(ApiResponse<object>.Ok(methods.OrderBy(m => m.DisplayOrder)));
}
```

### 5. Run the App

**On physical Android device:**
1. Enable Developer Options on your phone
2. Enable USB Debugging
3. Connect via USB
4. Select your device in Android Studio
5. Click ▶ Run

**On emulator:**
- Use `10.0.2.2` instead of `192.168.x.x` (emulator's alias for host PC)
- Example: `http://10.0.2.2:56359/`

---

## Project Structure

```
HealthcareApp/
├── app/src/main/
│   ├── java/com/healthcare/portal/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   ├── HealthcareApiService.kt   ← Retrofit endpoints
│   │   │   │   └── RetrofitClient.kt         ← Singleton HTTP client
│   │   │   └── model/
│   │   │       └── Models.kt                 ← Data classes
│   │   ├── ui/
│   │   │   ├── home/         HomeFragment + ViewModel
│   │   │   ├── booking/      DoctorsFragment, BookingFormFragment,
│   │   │   │                 PaymentFragment, ConfirmationFragment
│   │   │   ├── track/        TrackFragment + ViewModel
│   │   │   ├── appointment/  MyAppointmentsFragment
│   │   │   └── auth/         ProfileFragment
│   │   ├── utils/
│   │   │   ├── SessionManager.kt    ← SharedPreferences token storage
│   │   │   └── Extensions.kt        ← Helpers, status colors
│   │   ├── MainActivity.kt
│   │   └── HealthcareApp.kt
│   └── res/
│       ├── layout/     ← All XML layouts
│       ├── navigation/ ← nav_graph.xml
│       ├── drawable/   ← Backgrounds, icons
│       ├── menu/       ← bottom_nav_menu.xml
│       └── values/     ← colors, strings, themes
```

---

## EVC Plus Payment Flow

1. Patient selects EVC Plus as payment method
2. Enters their EVC phone number
3. App calls `POST /api/payment/hormuud/initiate` → sends USSD push to phone
4. Patient enters EVC password on their phone
5. App polls `GET /api/payment/hormuud/status/{referenceId}` every 5 seconds
6. On success → navigates to Confirmation screen

---

## Icons

The placeholder icons (`ic_home`, `ic_search`, etc.) are simple circles. Replace them with proper Material Icons:
1. In Android Studio: **File → New → Vector Asset**
2. Choose **Clip Art** and search for the icon name
3. Save to `res/drawable/`

Or download from [fonts.google.com/icons](https://fonts.google.com/icons).
