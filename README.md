# CitySeeker by Boa Apps

With this app you will be able to search among more than 200 cities in the world, locate them on a map and choose your favorites. It is about Android App Technical Testing with Kotlin, Compose, Retrofit and Hilt.

## Initial configuration

- Make an Mapbox account in https://account.mapbox.com/auth/signup/
- Go to Mapbox console and copy your new public token.
- In your `local.properties` file create add a line with this text: `mapboxToken={yourToken}`.
- In that line replace the string `{yourToken}` with your Mapbox public token.
- Use the latest stable version of Android Studio and download Android SDKs from API level 31 onwards.

## Features

- Make a GET request to gist.githubusercontent.com to get test data.
- Parse the JSON response and map it to Kotlin objects.
- Create a UI with Jetpack Compose that displays the data in a list.
- Implement MVVM architecture to structure the code appropriately.
- Manage UI states (loading, success, error) efficiently.
- Use of Kotlin coroutines and reactive programming principles.
- Proper handling of states and errors in the UI.
- Following clean architecture and best practices in Android.
- Searching and filtering places using a text input.
- Filter cities by the cities marked as favorites.
- Favorite city functionality with datastore persistence.
- Optimized room implementation to persist downloaded data.
- Optimized retrofit implementation for fast and compressed connection and download.
- Auto-adjust layout to switch between light and dark theme.
- Using animations.
- Mapbox implementation to locate the selected city.
- Auto-adjust layout for screen rotation.
- Offline-first (after download json). In any case, a local copy of the file is available for the first upload in case there is no response from the API. 

### Using gist.githubusercontent.com API RESTful

#### Specifically the route GET all cities

```cURL
curl --location --globoff 'https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json'
```

#### Usage/Examples

```json
[
  {
    "country": "AR",
    "name": "Provincia de Mendoza",
    "_id": 3844419,
    "coord": {
      "lon": -68.5,
      "lat": -34.5
    }
  }
]
```

## Next Steps

- Analytics & Monitoring.
- Benchmarking.
- Accessibility audit.
- Security audit.
- Performance audit.
- Add multi-language support.
- Improve code coverage with tests.
- Publish to Play Store.

## Considerations

- Initially, Ktor was attempted to be used with Realm, but better performance was achieved by replacing both with Room and Retrofit with compression and streaming.
- Decoupling of compose screens is prioritized over pagination.
- Pagination was postponed as it required unifying viewModels and/or modifying clean implementation to use cacheIn.
