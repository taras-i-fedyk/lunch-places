# Lunch Places

### Introduction

This is a compact Android app for finding nearby places where you can have lunch or something. You're free to perform the search by any text query.

The app has been created to experiment with different technologies and as a final project for Harvard University’s CS50 course. It's not for any distribution outside that context.

### Tech stack

* Kotlin

* Android SDK

* Material 3

* Jetpack Compose

* Accompanist Permissions

* Navigation Compose

* Glide

* Google Maps Compose

* Fused Location Provider

* Google Places SDK (New)

* Jetpack Preferences DataStore

* Hilt

* MVVM

* Clean Architecture

### Architectural notes

* Conceptually, the app’s architecture is shaped by the Clean Architecture paradigm. In the sense that the central layer of the app (business logic) is self-sufficient and independent from the external layers, while the external layers (storage and UI) depend on the central layer.

* The MVVM pattern is used to further organize efficient relationships between different layers and sublayers of the app. Also, the Hilt dependency injection framework is used to facilitate loose coupling and cohesiveness among different entities all over the app.

* Internally, the app relies on the Google Places API (New) to accomplish searches for lunch places.

* The app’s UI is built with the Jetpack Compose toolkit and follows the Material 3 design system.

### Key files overview

* __The business logic layer:__

  * __biz/data/GeoState__ - a global state to be shared between any interested parties across the app. It consists of two elements: the status of determining the current location and the status of searching for lunch places. The above statuses include all the relevant information like arguments, results, and error types.

  * __biz/GeoVM__ - a strategic ViewModel binding all the layers of the app together, in line with the MVVM pattern. This ViewModel serves as a producer of the GeoState. Since it is responsible for the following: determining the current location (based on the location permissions level) and searching for lunch places (based on the search settings). And it has to be noted that searching for lunch places always includes determining the current location.

  * __biz/util/ReplaceableLauncher__ - a utility for launching coroutines in such a way that a newly launched coroutine gracefully replaces the previously launched one. It is used by the GeoVM to avoid potential conflicts between multiple simultaneously running instances of the same operation.

  * __biz/LocationController__ - the entity directly responsible for determining the current location. Namely, a corresponding interface used by the GeoVM and its default implementation based on the Fused Location Provider.

  * __biz/SettingsRepo__ - an interface of the repository responsible for storing the app settings, which provides you with read/write access.

  * __biz/PlacesRepo__ - an interface of the repository responsible for storing all places, which allows you to search for lunch places that match certain criteria.

    The above two interfaces are related to storage. However, since those interfaces are used by the GeoVM belonging to the business logic layer of the app, they have been defined in the business logic layer as well. While their implementations have been defined in the storage layer as expected. (That way, we ensure the business logic layer is independent from the storage layer, in line with the Clean Architecture paradigm.)

* __The the storage layer:__

  * __store/SettingsRepoImpl__ - the default implementation of the biz/SettingsRepo interface, as a follow-up to the above. This implementation is based on using the Jetpack Preferences DataStore.

  * __store/PlacesRepoImpl__ - the default implementation of the biz/PlacesRepo interface, as a follow-up to the above. This implementation is based on using the Google Places SDK (New).

* __The UI layer:__

  * __ui/RootActivity__ - the app’s entry point and the container of its UI:

    * first of all, this entity monitors changes to the GeoState and delegates the main tasks to the GeoVM.

    * this entity manages each of the screens and the relationship between them. In short, there’s the Map screen and a navigation graph that overlays it. The navigation graph consists of the following four screens: Search, Settings, Details, and Proximity. Depending on the state of the current screen in the navigation graph, the Map screen can look and behave differently.

    * last but not least, this entity monitors if the location permissions have been granted to the app and assists the user in granting them when needed.

  * __ui/MapScreen__ - a screen displaying the map. Depending on its parameters that are changed dynamically, this screen has the following extra features:

    * be visible or not.

    * display the current location on the map.

    * allow the user to determine the current location ad hoc.

    * display the destination on the map, without displaying the origin on it.

    * automatically or per the user’s request, position the map’s camera so that the current location is in the center of the screen or so that the distance between the origin and the destination is fully visible.

  * __ui/SearchScreen__ - a screen allowing the user to search for lunch places by any text query and view the search results as a list.

    The user can navigate to the Details screen by clicking a search results item. Also, the user can navigate to the Settings screen by clicking a corresponding icon in the search bar.

    When no search is underway, this screen consists of a search bar only. In such a case, the Map screen is visible behind this screen and supports all the features related to the current location. Otherwise, the Map screen is not visible behind this screen.

  * __ui/SettingsScreen__ - a screen allowing the user to configure search settings like the ranking criterion and the preferred radius.

    The Map screen is not visible behind this screen.

  * __ui/DetailsScreen__ - a screen allowing the user to view the details of a found lunch place.

    The user can navigate to the Proximity screen by clicking a corresponding icon in the top bar.

    The Map screen is not visible behind this screen.

  * __ui/ProximityScreen__ - a screen allowing the user to view the distance between the location at which the search has been performed (the origin) and a found lunch place (the destination).

    This screen consists of a top bar only. The Map screen is visible behind this screen and is characterized by the following: it displays the current location on the map and supports all the features related to the destination.

### Prerequisites

* It will be possible to generate debug and release builds of the app as long as they are signed with valid certificates. Currently, debug and release builds are signed with certificates valid until the following dates: Fri Jul 28 01:36:43 EEST 2051 and Mon Oct 02 14:33:44 EEST 2051, respectively.

  After those dates, you can obtain the new certificates if any. To do that, you’d have to redownload the app’s source code.

* The app can be installed on devices carrying Android 7 or later.

* The app’s features related to the map and lunch places will work successfully as long as the Google Maps API Key and the Google Places API Key integrated into the app are valid. Currently, it’s not planned to invalidate those API Keys and generate new ones instead. However, the policy may change in case of suspected malicious use of those API Keys (for example, when they take part in too much traffic).

  By the way, it has to be noted that the above API Keys have been integrated into the app in such a way that it's difficult to steal them for use elsewhere.

  Nonetheless, if the app keeps signaling about an invalid configuration, it’s likely due to issues with the API Keys' validity. In such a case, it’d make sense to ensure the API Keys integrated into the app are up-to-date. To do that, you’d have to redownload the app’s source code, rebuild the app, and reinstall it on the device.

* For the app to be useful, it should be able to determine the current location. In such a case, the following conditions are required:

  * the version of Android carried by the device includes appropriate Google APIs. (This is usually the case.)

  * the location services have been enabled on the device. (This is usually the case. However, it can be configured via the stock Settings app.)

  * the location services have been “initialized” on the device. (This is usually the case. However, it can be ensured by running the stock Maps app and waiting until it has determined the current location, which is typically needed for a new device only.)

  * the location permissions have been granted to the app on the device. (The app will assist you in granting such permissions.)

### Simplifications

* Ideally, the app would have to communicate with a dedicated server that encapsulates a direct interaction with the Google Places API (New). However, the Google Places SDK (New) as a convenient wrapper around the above API has been integrated into the app instead. As a result, the following trade-offs take place:

  * the Google Places API Key has been integrated into the app, which is less secure and flexible than having it encapsulated in the dedicated server.

  * the search results can contain at most 20 items.

* The app’s Details screen contains a fairly limited amount of information. Also, the Proximity screen only shows how close the found lunch place is, without suggesting routes to get there.

* The app’s UI has been optimized only for phones, not for tablets.

* In its current state, the app doesn't fully comply with the Terms of Service of the Google Maps API and the Google Places API (for example, in the context of familiarizing the user with the text of those Terms of Service and in the context of ensuring the required attributions are displayed).

  Also, the app doesn't familiarize the user with the third-party open-source licenses on which its functioning is based to one degree or another.

  However, it should be fine taking into account what purposes the app has been created for (see the Introduction section).