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
#include <Arduino.h>
#include <HX711.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
// HX711 Pin Configuration
#define LOADCELL_DOUT_PIN_1 32
#define LOADCELL_SCK_PIN_1 33
#define LOADCELL_DOUT_PIN_2 25
#define LOADCELL_SCK_PIN_2 26
#define LOADCELL_DOUT_PIN_3 27
#define LOADCELL_SCK_PIN_3 14
HX711 scale1;
HX711 scale2;
HX711 scale3;
// BLE Configuration
#define SERVICE_UUID "12345678-1234-5678-1234-56789abcdef0"
#define CHARACTERISTIC_UUID "12345678-1234-5678-1234-56789abcdef1"
BLECharacteristic pCharacteristic;
bool deviceConnected = false;
// Function to read weight from the sensors
void readWeights() {
float weight1 = scale1.get_units(10);
float weight2 = scale2.get_units(10);
float weight3 = scale3.get_units(10);
// Create a string to send over BLE
String weightData = String(weight1) + "," + String(weight2) + "," + String(weight3);
// Send the weight data over BLE
pCharacteristic->setValue(weightData.c_str());
pCharacteristic->notify();
}
class MyServerCallbacks : public BLEServerCallbacks {
void onConnect(BLEServer pServer) {
deviceConnected = true;
}
void onDisconnect(BLEServer pServer) {
deviceConnected = false;
}
};
void setup() {
Serial.begin(115200);
// Initialize HX711
scale1.begin(LOADCELL_DOUT_PIN_1, LOADCELL_SCK_PIN_1);
scale2.begin(LOADCELL_DOUT_PIN_2, LOADCELL_SCK_PIN_2);
scale3.begin(LOADCELL_DOUT_PIN_3, LOADCELL_SCK_PIN_3);
// Set the scale calibration factor (adjust this based on your calibration)
scale1.set_scale(2280.f);
scale2.set_scale(2280.f);
scale3.set_scale(2280.f);
// Initialize BLE
BLEDevice::init("WeightSensor");
pServer->setCallbacks(new MyServerCallbacks());
BLEService pService = pServer->createService(SERVICE_UUID);
pCharacteristic = pService->createCharacteristic(
CHARACTERISTIC_UUID,
BLECharacteristic::PROPERTY_NOTIFY
);
pCharacteristic->addDescriptor(new BLE2902());
pService->start();
BLEAdvertising pAdvertising = pServer->getAdvertising();
pAdvertising->start();
Serial.println("BLE Ready! Connect to your app.");
}
void loop() {
if (deviceConnected) {
readWeights();
delay(1000); // Send data every second
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


