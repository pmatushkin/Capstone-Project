# Still In Memphis
Still In Memphis is a capstone project for the Android Developer Nanodegree program developed by [Udacity](https://www.udacity.com).

## Description
Still In Memphis (see [xkcd](https://xkcd.com/281/)) is an app built for the Android phones and tablets. The app manages the tracking numbers of the USPS packages, and communicates with the USPS Web Tools API (USPS API) to allow package tracking.

## Pre-requisites
* OkHttp 3.2.0
* The valid USPS Web Tools API key ([instructions](https://www.usps.com/business/web-tools-apis/welcome.htm))
* The valid Google Maps Android API key ([instructions](https://developers.google.com/maps/documentation/android-api/))

## Instructions
Still In Memphis uses two APIs, both of which require a key.

Before compiling the project, please obtain an USPS API key following the link to the [instruction page](https://www.usps.com/business/web-tools-apis/welcome.htm). The USPS API key must be then placed into [/app/src/main/res/values/strings.xml](https://github.com/pmatushkin/Capstone-Project/tree/master/app/src/main/res/values) under the key `usps_api_key`

Do the same for a Google Maps Android API key following this link to the [instruction page](https://developers.google.com/maps/documentation/android-api/). The Google Maps Android API key must be placed into [/app/src/main/res/values/strings.xml](https://github.com/pmatushkin/Capstone-Project/tree/master/app/src/main/res/values) under the key `maps_api_key`

## Use cases
* The user enters a tracking number into the app. The app communicates the tracking number to the USPS API and retrieves a set of tracking events, or a single error event. The entered tracking number is displayed on the Tracking Number List screen along with the description of the most recent event, and a package icon that represents the status of the tracking number. The black icon represents the packages in progress, the green icon represents the delivered packages, and the red icon represents the error reported by the USPS API.

* The retrieved tracking events (black or green package icons), or the error event (red package icon), represent the current state of the tracking number on the Tracking Number Detail screen. The events are sorted from the most recent to the most distant. The approximate address of the most recent event, as generated and returned by the USPS API, is displayed on the map. The error event doesn’t have any address associated with it, and the detail screen doesn’t display the map. In case of geocoding failure the detail screen displays the map of the United States.

* The app periodically communicates the active tracking numbers to the USPS API to retrieve the latest tracking events. If the description of the most recent tracking event contains the word “Delivered”, the package is marked as delivered, and the tracking number icon turns green. The active tracking numbers are the ones with the black icons (package in progress) or the red icons (the USPS API returned an error). The app stops communicating the tracking number to the USPS API if the corresponding package is delivered or archived.

* The user can archive the previously entered tracking number. The app will stop tracking it, the number will disappear from the Active view, and appear on the Archive view. The tracking number icon will turn gray.

* The user can move the previously archived tracking number back to the Active view. The app will resume updating it.

* The user can delete the previously entered tracking number either from the Active view, or from the Archive view. The tracking number and all its events will be removed from the app.

## Screenshots

## Acknowledgements
* Information provided by [USPS](http://www.usps.com)
* Inspired by, widget header provided by, [xkcd](https://xkcd.com/281/)
* Informed by [Sunshine](https://github.com/udacity/Advanced_Android_Development/tree/7.05_Pretty_Wallpaper_Time)
* Icons provided by [Google Design](https://design.google.com/icons/), [Material Design Icons](https://materialdesignicons.com/), and personally [Austin Andrews](https://twitter.com/Templarian)

## Notes
* This repository includes the keystore and the passwords used for signing the APK. Both the keystore and the passwords are generated specifically for this app, and included only to demonstrate the capability of the project to successfully assemble the signed release APK. Please don’t use them for anything else. The keystore is located in [keystore/capstone_keystore.jks](https://github.com/pmatushkin/Capstone-Project/tree/master/keystore)

* This repository includes the signed release APK of the app as of [b14cb752d052a857a879ecf646489c5a54cde12f](https://github.com/pmatushkin/Capstone-Project/commit/b14cb752d052a857a879ecf646489c5a54cde12f). The APK is located in [/apk/app-release.apk](https://github.com/pmatushkin/Capstone-Project/tree/master/apk)

* These tracking numbers are provided for the purposes of testing. Please keep in mind that these tracking numbers and the associated events are maintained by USPS, and can be discontinued at any time.
  * 9405503699300270004035
  * 9405803699300222655286
  * 9241990104791668956989
