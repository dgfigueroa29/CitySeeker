# CitySeeker by Boa Apps

With this app you will be able to search among more than 200 cities in the world, locate them on a
map and choose your favorites. It is about Android App Technical Testing with Kotlin, Compose, Ktor
and Hilt.

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

