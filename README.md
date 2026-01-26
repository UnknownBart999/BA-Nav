# BA-Nav: Building Augmented Navigation
## Viewer app
Enter BA-Nav/Viewer directory before running any of the following commands
### Requirements
- Dart/Flutter
- Android Studio (for Android build)
- Visual Studio 2022 Build Tools for C++ Desktop Development (for Windows build)
### How to check requirements
```
flutter docker

[√] Flutter
[√] Visual Studio - develop Windows apps (for Windows build)
[√] Android Studio (for Android build)
```
### Running
For android, plug an android device with developer mode enabled to your computer, or use an emulator.
```
flutter run -d <windows|android>
```
### Building
```bash
# Windows build
flutter build windows
# Android build
flutter doctor --android-licenses
flutter build apk
flutter install # Optionally install app onto connected android device
```
## Map Maker app