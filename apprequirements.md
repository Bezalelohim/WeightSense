
Project Overview

This project is an Android application that communicates with IoT devices (specifically an ESP32) over Bluetooth Low Energy (BLE). The app uses Jetpack Compose for the UI and Room for local data storage. It will primarily be used to retrieve sensor data (e.g., weight readings) from a device and store it under user profiles. Users can create profiles to manage and view data from their IoT devices. The app features three main pages: Home, Devices, and Settings, with a bottom bar for navigation.

Feature List

Core Features:

    Bluetooth Low Energy (BLE) Communication
        Scan for nearby BLE devices and pair with an ESP32.
        Send and receive data (sensor readings such as weight) to and from the device.
        Display real-time sensor data or the last synced information when the device is not connected.
        Service UUID and Characteristic UUID management for BLE communication.

    User Profiles
        Users can create and manage profiles.
        Devices are saved under each profile, allowing easy switching between users and their devices.
        When no devices are saved under a profile, display a message indicating no devices are available.
        When no profile is selected, prompt the user to create or select a profile.

    Local Data Storage with Room
        Store BLE device information and sensor readings locally.
        Enable users to view historical data of sensor readings when a device is not connected.
        Sync data when the device reconnects and display the last updated time on the home screen.

    Three Pages with Bottom Bar Navigation
        Home Page:
            Displays sensor data of the selected device within the user’s profile.
            Shows the last synced information when no active connection is available.
            If no profile is selected, prompt the user to create/select a profile.
        Devices Page:
            Shows a list of devices connected to the user's profile.
            If no devices are available, display an appropriate message.
            Allow scanning, connecting, and data transfer functionality.
        Settings Page:
            Allows users to modify preferences and notification settings.

    Device Management
        Allow users to add and remove devices within their profile.
        Show the device’s last sync time and latest sensor data when the device is unavailable.

    Permissions Handling with Accompanist
        Handle runtime permissions (e.g., location, Bluetooth) using the Accompanist permissions library.
        Ensure the user is prompted to grant necessary permissions for BLE communication.
        Provide a rationale and graceful fallback if permissions are denied.

BLE Scanning, Connecting, and Data Transfer:

    Scanning for BLE Devices:
        On the Devices Page, users will find a Scan for Devices button.
        When pressed, the app initiates a BLE scan for nearby Bluetooth devices.
        Discovered devices will be listed, displaying their name (or MAC address) and signal strength (RSSI).
        The user can select a device from the list.

    Selecting a Service UUID and Characteristic UUID:
        After selecting a device, the app will discover the available services and characteristics provided by the BLE device.
        Users will select or input the relevant Service UUID and Characteristic UUID, which correspond to the specific data (e.g., weight sensor readings) the device will provide.
        The app will show available services and characteristics if they are available for selection, or it will allow manual input of the UUIDs.

    Connecting to the Device:
        Once the device and appropriate UUIDs are selected, the Connect button becomes available.
        Upon tapping Connect, the app establishes a BLE connection and subscribes to the chosen Service UUID and Characteristic UUID for data exchange.
        If successful, the app will display a "Connected to [device name]" message along with the selected UUIDs.
        In case of connection failure, the app will notify the user with an error message.

    Data Transfer (Send/Receive):
        Once connected, the Send and Receive buttons will become available:
            Send: Sends data (e.g., configuration commands or a request for weight data) to the device via the selected Characteristic UUID.
            Receive: Reads data (e.g., weight sensor readings) from the BLE device via the selected Characteristic UUID.
            The received data will be displayed on the Home Page.

    Handling Connection Loss:
        If the BLE connection is lost, the app will display a "Connection lost" message and the Send and Receive buttons will be disabled.
        Users can attempt to reconnect using the Connect button.

Important BLE Concepts:

    Service UUID: Identifies a BLE service provided by the device (e.g., a weight sensor service).
    Characteristic UUID: Identifies a specific piece of data or function within a BLE service (e.g., the weight reading characteristic).

Dependencies
Jetpack Compose Libraries (UI)

    Compose UI: Core libraries for the declarative UI.
        implementation "androidx.compose.ui:ui:<latest-version>"
        implementation "androidx.compose.material:material:<latest-version>"
        implementation "androidx.compose.ui:tooling:<latest-version>" (for UI previews)

Jetpack Navigation (Bottom Bar Navigation)

    Navigation Compose: For handling the bottom bar navigation.
        implementation "androidx.navigation:navigation-compose:<latest-version>"

Room (Local Database)

    Room Persistence Library: For local storage of user profiles and BLE device data.
        implementation "androidx.room:room-runtime:<latest-version>"
        annotationProcessor "androidx.room:room-compiler:<latest-version>"
        implementation "androidx.room:room-ktx:<latest-version>"
        
	Room Persistence Library: For local storage of user profiles and BLE device data.
    	implementation "androidx.room:room-runtime:<latest-version>"
    	ksp "androidx.room:room-compiler:<latest-version>" (Use KSP for Room annotation processing)

Bluetooth Low Energy (BLE)

    Android Bluetooth Library: For managing BLE connections and communication.
        implementation "androidx.bluetooth:bluetooth:<latest-version>"

Lifecycle Management

    ViewModel and LiveData: For managing UI-related data lifecycle-aware.
        implementation "androidx.lifecycle:lifecycle-viewmodel-compose:<latest-version>"
        implementation "androidx.lifecycle:lifecycle-livedata-ktx:<latest-version>"

Preferences

    DataStore: For storing user preferences and settings.
        implementation "androidx.datastore:datastore-preferences:<latest-version>"

Hilt: For dependency injection to improve scalability and manage code dependencies.

    implementation "com.google.dagger:hilt-android:<latest-version>"
    ksp "com.google.dagger:hilt-compiler:<latest-version>" (Use KSP instead of KAPT)



Permissions Handling (Accompanist)

    Accompanist Permissions: For managing runtime permissions in Jetpack Compose.
        implementation "com.google.accompanist:accompanist-permissions:<latest-version>"
        Documentaion:

	Compose Versions

	Each release outlines which version of the Compose UI libraries it depends on. Currently, Accompanist is releasing multiple versions for different versions of Compose:
	Compose Version	Maven Central Location
	Compose 1.0 (1.0.x)	Maven Central
	Compose 1.1 (1.1.x)	Maven Central
	Compose UI 1.2 (1.2.x)	Maven Central
	Compose UI 1.3 (1.3.x)	Maven Central
	Compose UI 1.4 (1.4.x)	Maven Central
	Compose UI 1.5 (1.5.x)	Maven Central
	Compose UI 1.6 (1.6.x)	Maven Central
	Compose UI 1.7 & 1.8 (1.7.x)	Maven Central

	For stable versions of Compose, Accompanist uses the latest stable version of the Compose compiler. For non-stable versions (alpha, beta, etc.), 
	Accompanist uses the latest compiler at the time of release.
	Permissions Support

	Accompanist provides a library for handling Android runtime permissions support for Jetpack Compose.
	Warning:

	The permission APIs are currently experimental, and they could change at any time. All of the APIs are marked with the @ExperimentalPermissionsApi annotation.
	Usage
	rememberPermissionState and rememberMultiplePermissionsState APIs

	    The rememberPermissionState(permission: String) API allows you to request a certain permission from the user and check the permission status.
	    rememberMultiplePermissionsState(permissions: List<String>) offers the same, but for multiple permissions at the same time.

	Both APIs expose properties for you to follow the workflow as described in the permissions documentation.
	Caution:

	The call to the method that requests permission from the user (e.g., PermissionState.launchPermissionRequest()) needs to be invoked from a non-composable scope. 
	For example, from a side-effect or a non-composable callback such as a Button's onClick lambda.
	Code Example

The following code demonstrates how to request permissions using Accompanist:
```
	@OptIn(ExperimentalPermissionsApi::class)
	@Composable
	private fun FeatureThatRequiresCameraPermission() {
    	// Camera permission state
    	val cameraPermissionState = rememberPermissionState(
        	android.Manifest.permission.CAMERA
    	)

    	if (cameraPermissionState.status.isGranted) {
       	 	Text("Camera permission Granted")
    	} 	else {
        	Column {
            	val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                	// If the user has denied the permission but rationale can be shown,
                	// gently explain why the app requires this permission.
                	"The camera is important for this app. Please grant the permission."
            		} else {
                	// If it's the first time the user lands on this feature, or the user
                	// doesn't want to be asked again for this permission, explain that
                	// the permission is required.
                	"Camera permission required for this feature to be available. Please grant the permission."
            	}
            	Text(textToShow)
            	Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                	Text("Request permission")
            	}
        	}
    	}
	}
```

General Rules

    Follow MVVM Architecture
        The project must adhere to the Model-View-ViewModel (MVVM) architecture.
        This ensures a clear separation of concerns:
            Model: Responsible for handling the business logic and data (including BLE communication and Room database interactions).
            View: The Jetpack Compose UI layer that displays the data to the user.
            ViewModel: Serves as a mediator between the Model and the View, holding UI-related data and logic, and maintaining lifecycle awareness to ensure data persists through configuration changes.
        Following MVVM will make the app more scalable, testable, and maintainable.

    Dependency Injection with Dagger Hilt
        Use Hilt for dependency injection to keep the codebase clean and modular, facilitating testing and future expansion.

    Permissions Handling with Accompanist
        Manage runtime permissions efficiently by using the Accompanist library. Ensure that permission prompts are presented appropriately, and handle denied permissions with fallbacks or explanations.

    Separation of Concerns
        Ensure each component (BLE communication, database management, and UI) is independent and reusable across the app.

    Utility Functions in the Utils Folder
        All utility functions such as string processing, data formatting, or other helper methods should be placed in a dedicated utils folder.
        This keeps the project organized and ensures that utility functions are reusable and not tied to specific components.

    Error Handling
        Handle BLE connection errors gracefully, providing user feedback (e.g., notifications or prompts) in case of connection failures or unavailable data.

    Asynchronous Programming
        Utilize Kotlin Coroutines for handling BLE communication, database operations, and other asynchronous tasks efficiently.

    Material Design
        Follow Google’s Material Design guidelines for UI/UX to ensure a visually appealing and user-friendly app interface.
        
    Current File structure:
    
├── build.gradle.kts
├── DeviceScreen.png
├── HomeScreen.png
├── proguard-rules.pro
├── SettingsScreen.png
└── src
    ├── androidTest
    │   └── java
    │       └── com
    │           └── example
    │               └── weightsense
    │                   └── ExampleInstrumentedTest.kt
    ├── main
    │   ├── AndroidManifest.xml
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── weightsense
    │   │               ├── data
    │   │               │   ├── ble
    │   │               │   │   ├── BleManager.kt
    │   │               │   │   └── BleService.kt
    │   │               │   ├── dao
    │   │               │   │   └── BleDeviceDao.kt
    │   │               │   ├── Database.kt
    │   │               │   ├── model
    │   │               │   │   ├── BleDevice.kt
    │   │               │   │   └── Converters.kt
    │   │               │   └── repository
    │   │               │       ├── BleDeviceRepository.kt
    │   │               │       └── UserProfileRepository.kt
    │   │               ├── di
    │   │               │   └── AppModule.kt
    │   │               ├── domain
    │   │               ├── MainActivity.kt
    │   │               ├── ui
    │   │               │   ├── common
    │   │               │   │   └── BlePermissionHandler.kt
    │   │               │   ├── device
    │   │               │   │   ├── DeviceScreen.kt
    │   │               │   │   └── DeviceViewModel.kt
    │   │               │   ├── home
    │   │               │   │   ├── HomeScreen.kt
    │   │               │   │   └── HomeViewModel.kt
    │   │               │   ├── navigation
    │   │               │   │   ├── BottomNavigationBar.kt
    │   │               │   │   └── Navhost.kt
    │   │               │   ├── settings
    │   │               │   │   ├── SettingsScreen.kt
    │   │               │   │   └── SettingsViewModel.kt
    │   │               │   └── theme
    │   │               │       ├── Color.kt
    │   │               │       ├── Theme.kt
    │   │               │       └── Type.kt
    │   │               └── utils
    │   └── res
    │       ├── drawable
    │       │   ├── ic_launcher_background.xml
    │       │   └── ic_launcher_foreground.xml
    │       ├── mipmap-anydpi
    │       │   ├── ic_launcher_round.xml
    │       │   └── ic_launcher.xml
    │       ├── mipmap-hdpi
    │       │   ├── ic_launcher_round.webp
    │       │   └── ic_launcher.webp
    │       ├── mipmap-mdpi
    │       │   ├── ic_launcher_round.webp
    │       │   └── ic_launcher.webp
    │       ├── mipmap-xhdpi
    │       │   ├── ic_launcher_round.webp
    │       │   └── ic_launcher.webp
    │       ├── mipmap-xxhdpi
    │       │   ├── ic_launcher_round.webp
    │       │   └── ic_launcher.webp
    │       ├── mipmap-xxxhdpi
    │       │   ├── ic_launcher_round.webp
    │       │   └── ic_launcher.webp
    │       ├── values
    │       │   ├── colors.xml
    │       │   ├── strings.xml
    │       │   └── themes.xml
    │       └── xml
    │           ├── backup_rules.xml
    │           └── data_extraction_rules.xml
    └── test
        └── java
            └── com
                └── example
                    └── weightsense
                        └── ExampleUnitTest.kt

