# WeightSense - BLE Weight Monitoring App

A modern Android application for monitoring weight measurements using Bluetooth Low Energy (BLE) technology. The app connects to compatible weight sensing devices and provides real-time weight data monitoring and tracking.

## 🛠️ Technologies Used

- **Jetpack Compose** - Modern Android UI toolkit
- **Bluetooth Low Energy (BLE)** - For device communication
- **Kotlin Coroutines & Flow** - For asynchronous operations
- **Dagger Hilt** - Dependency injection
- **Room Database** - Local data persistence
- **Material Design 3** - UI components and theming
- **Accompanist** - Permission handling
- **Android Architecture Components** - ViewModel, Navigation

## 🔧 Key Features

### BLE Communication
- Scan and discover BLE weight sensing devices
- Real-time weight data monitoring
- Automatic reconnection handling
- Permission management for Bluetooth operations

### User Interface
- Home Screen: Real-time weight display with visual indicator
- Devices Screen: BLE device management
- Settings Screen: App configuration and user preferences

### Data Management
- Local storage of weight measurements
- User profile management
- Weight unit conversion (kg/lbs)
- Theme customization (Light/Dark/System)

## 📱 Compatibility

- Android 8.0 (API 26) and above
- Requires Bluetooth Low Energy capable device
- Compatible with BLE weight sensing devices

## 🔒 Permissions

- Bluetooth (BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
- Location (for Android < 12)
- Bluetooth Admin (for older Android versions)

## 📱 Screen UI

![Alt text](screenui/home.jpeg)
:
![Alt text](screenui/settings.jpeg)
:
![Alt text](screenui/scan.jpeg)


-------------------------------------------------------------------------------------------------------------------------------
## 📡 ESP32 Weight Sensor Implementation

This section outlines the implementation of an ESP32-based weight sensor system that reads weight data from three HX711 load cells and transmits the data over Bluetooth Low Energy (BLE) to the WeightSense Android application.

### 🛠️ Required Components

- **ESP32 Development Board**
- **HX711 Load Cell Amplifier**
- **Three Load Cells (e.g., bathroom scale sensors)**
- **Jumper Wires**
- **Breadboard (optional)**

### 📦 Libraries Required

Make sure to install the following libraries in your Arduino IDE:
- **HX711**: Library for interfacing with the HX711 load cell amplifier.
- **BLE**: The BLE library is included with the ESP32 board package.

### 📋 ESP32 Code Example

```cpp
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <Arduino.h>
#include "HX711.h"

#define calibration_factor -7050.0  // Obtained using the SparkFun_HX711_Calibration sketch

#define LOADCELL_DOUT_PIN  21
#define LOADCELL_SCK_PIN   22

// BLE UUIDs used in your Android app
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

HX711 scale;

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;
float weightValue = 0.0;

// Debug flag
bool DEBUG = true;

// Forward declaration of helper function
void float32_to_bytes(float value, uint8_t *bytes);

class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) override {
    deviceConnected = true;
    if (DEBUG) Serial.println("Device connected!");
  };

  void onDisconnect(BLEServer* pServer) override {
    deviceConnected = false;
    if (DEBUG) Serial.println("Device disconnected!");
    // Restart advertising when disconnected
    BLEDevice::startAdvertising();
  }
};

void setup() {
  Serial.begin(115200);
  delay(1000); // Allow time for serial port initialization

  // Initialize the scale
  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  scale.set_scale(calibration_factor);
  scale.tare(); // Reset the scale to 0

  Serial.println("Readings:");
  if (DEBUG) Serial.println("Starting BLE setup...");

  // Initialize BLE
  BLEDevice::init("LPG-WeightSense");
  if (DEBUG) Serial.println("BLE Device initialized");

  // Create the BLE Server and set callbacks
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  if (DEBUG) Serial.println("BLE Server created");

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);
  if (DEBUG) Serial.println("BLE Service created");

  // Create BLE Characteristic with READ and NOTIFY properties
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  if (DEBUG) Serial.println("BLE Characteristic created");

  // Add descriptor to the characteristic
  pCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();
  if (DEBUG) Serial.println("BLE Service started");

  // Set up advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();

  if (DEBUG) Serial.println("Setup complete! Waiting for connections...");
}

void loop() {
  if (deviceConnected) {
    // Read the sensor value once and store it
    weightValue = scale.get_units();
    
    if (weightValue < 0) {
            weightValue = 0;
        }

    Serial.print("Readings: ");
    Serial.println(weightValue, 1);

    // Convert float to bytes (little-endian)
    uint8_t bytes[4];
    float32_to_bytes(weightValue, bytes);

    // Update the BLE characteristic and notify the client
    pCharacteristic->setValue(bytes, 4);
    pCharacteristic->notify();

    if (DEBUG) {
      Serial.print("Weight value sent: ");
      Serial.println(weightValue);
    }
    
    // Update every ten seconds
    delay(2000);
  }
  
  // Small delay to prevent watchdog issues
  delay(20);
}

// Helper function to convert a float to a byte array (Little Endian)
void float32_to_bytes(float value, uint8_t *bytes) {
  union {
    float float_value;
    uint8_t byte_value[4];
  } converter;
  
  converter.float_value = value;
  
  // Copy bytes in little-endian order
  for (int i = 0; i < 4; i++) {
    bytes[i] = converter.byte_value[i];
  }
}

```
### ⚙️ Wiring Diagram

Connect the HX711 modules to the ESP32 according to the defined pins in the code:

- **Load Cell 1**: 
  - DOUT -> GPIO 32
  - SCK -> GPIO 33
- **Load Cell 2**: 
  - DOUT -> GPIO 25
  - SCK -> GPIO 26
- **Load Cell 3**: 
  - DOUT -> GPIO 27
  - SCK -> GPIO 14

### 📏 Calibration

Make sure to calibrate your HX711 scales by adjusting the `set_scale` factor according to your specific load cells.

### 📱 Testing

Upload the code to your ESP32, and use a BLE scanner app on your Android device to connect and receive weight data. You can then integrate this data into your existing Android application.


## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.



