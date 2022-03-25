## MVVMNewsApp

## Technologies Used:

- Kotlin
- Retrofit
- Gson Converter
- LiveData
- KT Coroutines
- Room persistence library
- Jetpack Navigation Component
- MVVM
- BottomNavigation
- API from: newsapi.org

## About

- An MVVM news app that gets news feed from an API through Retrofit from a specified country. RoomDB is used to cache locally the API request (NewsFeed) into a local SQL database.
- LiveData is used to observe and respond to configuration changes like screen rotation, and changes in the fetched data through Retrofit from the remote data source, asynchronously using Coroutines.
- Ability to handle WiFi, Mobile and Ethernet Internet connectivity issues.
- Ability to search news. App displays the news in real-time from RoomDB based on the searh query entered by the user.
- A breaking news fragment, having a scrolling list with pagination applied. Each article opens in a WebView when clicking. User can save an article and deleted then undo.
