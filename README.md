# HSL Timetable Android Application

HSL Timetable is a simple Android application that shows arrival times of selected HSL routes (buses,
trains or metro) at selected stops in Helsinki metropolitan area. It obtains and processes realtime
data from HSL [Open Data API platform](https://digitransit.fi/en/developers/).

## Installation
Clone this repository and import into **IntelliJ IDEA** or **Android Studio**
```bash
git clone git@github.com:ilinpetar/HslTimetable.git
```

## Configuration
### API keys
The use of the HSL Open Data (Digitransit) production APIs requires registration and use of API keys.
The procedure for acquiring keys is detailed [here](https://digitransit.fi/en/developers/api-registration/)


## Build variants
Use the IntelliJ IDEA/Android Studio *Build Variants* button to choose between **production** and
**staging** flavors combined with debug and release build types


## Generating signed APK
From IntelliJ IDEA/Android Studio:
1. ***Build*** menu
2. ***Generate Signed App Bundle / APK...***
3. Fill in the keystore information *(you only need to do this once manually and then let IntelliJ
IDEA/Android Studio remember it)*

## Maintainers
This project is maintained by:
* [Petar Ilin](https://github.com/ilinpetar)


## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Push your branch (git push origin my-new-feature)
5. Create a new Pull Request