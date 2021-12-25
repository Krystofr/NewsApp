## MVVMNewsApp

## Technologies Used:

- Kotlin
- Retrofit
- Gson Converter
- LiveData
- Coroutines
- Room persistence
- Jetpack Navigation Component
- MVVM
- BottomNavigation

## About

- An MVVM news app that gets news feed from an API through Retrofit from a specified country. RoomDB is used to cache locally the API request (NewsFeed) into a local SQL database.
- LiveData is used to observe and respond to configuration changes like screen rotation, and changes in the fetched data through Retrofit from the remote data source, asynchronously using Coroutines.
- Ability to handle WiFi, Mobile and Ethernet Internet connectivity issues.
