# Assignment App for bus and metro stations

An Android application that demonstrates modern Android development practices. The app fetches transit stop data (Bus/Metro), stores it locally for offline access, and displays locations on a map.

## 📱 Features

*   **Data Fetching:** Retrieves stop data from a remote API.
*   **Local Caching:** Persists data using **Room Database** for offline capability.
*   **Background Sync:** Uses **WorkManager** to keep data updated in the background.
*   **Maps Integration:** Displays stops on **Google Maps**.
*   **Stop Details:** Differentiates between Bus and Metro stops.

## 🛠 Tech Stack

*   **Language:** Java
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **UI Components:**
    *   Material Design components
    *   ConstraintLayout
    *   SwipeRefreshLayout
    *   ViewBinding
*   **Networking:**
    *   [Retrofit2](https://square.github.io/retrofit/) - REST Client
    *   [OkHttp3](https://square.github.io/okhttp/) - HTTP Client & Interceptors
    *   [GSON](https://github.com/google/gson) - JSON Serialization
*   **Jetpack Libraries:**
    *   **Room:** Local SQLite abstraction.
    *   **WorkManager:** Deferrable background work.
    *   **Navigation Component:** In-app navigation.
    *   **Lifecycle / LiveData / ViewModel:** lifecycle-aware components.
*   **Google Services:**
    *   Play Services Maps (Google Maps SDK)
    *   Play Services Location


## 🚀 Setup & Installation

1.  **Clone the repository:**

2.  **Open in Android Studio:**
    Open the project using the latest version of Android Studio.

3.  **Configure Google Maps API Key:**
    *   Get an API Key from the [Google Cloud Console](https://console.cloud.google.com/).
    *   Open `local.properties`.
    *   Add your key as `MAPS_API_KEY`= `YOUR_API_KEY`:


4.  **Build and Run:**
    Sync Gradle files and run the app on an Emulator or Physical Device.

## ⚠️ Common Issues

**Crash on specific data input:**
Some API responses might send malformed strings.

## Note 
Used the internet for 
* calculation logic.
* generation of readme file 
* Copied the required code from my previous applications.

## 👤 Author

**Sachin Mandhare**

---
    