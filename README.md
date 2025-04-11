# 📱 MVVMNewsApp – News Reader Android App

A modern Android news application built with **Kotlin**, following the **MVVM + Clean Architecture** pattern. It fetches and displays real-time news articles from multiple categories using the [NewsAPI.org](https://newsapi.org/) service, supports in-app article viewing via a custom **WebView**, and enables sharing news across platforms.

---

## 🚀 Features

- 📰 Fetches breaking news from **NewsAPI.org**
- 🗂️ Categorized content displayed via **TabLayout + ViewPager2**
- 🌐 Custom **WebView** to display full article content
- 📤 One-tap sharing to other apps
- 🔁 Offline cache and error handling
- 📲 Responsive UI optimized for various screen sizes

---

## 🧱 Tech Stack & Libraries

| Layer         | Technology                                      |
|---------------|-------------------------------------------------|
| Language      | Kotlin                                          |
| Networking    | Retrofit, OkHttp, Gson                          |
| Architecture  | MVVM + Clean Architecture                       |
| UI            | XML, TabLayout, ViewPager2, BottomNavigation    |
| Navigation    | Jetpack Navigation Component                    |
| Async Ops     | Kotlin Coroutines, LiveData                     |
| DI            | Dagger-Hilt                                     |
| API Source    | [NewsAPI.org](https://newsapi.org/)             |

---

## 🧠 Architecture Overview

This app follows **Clean Architecture** principles with clear separation between:

- `data` – Repository implementations, remote data source (Retrofit)
- `domain` – UseCases and models
- `presentation` – ViewModels, UI components, and navigation

