# LSPosed — Samsung IMS Patcher

**Short description**

LSPosed module to unlock Samsung-restricted carrier-related features on rooted Galaxy devices (VoWiFi, VoLTE, ViLTE, RCS, SMS over IP).

---

## Overview

This repository contains an LSPosed module that restores or unlocks certain IMS-related features that Samsung has restricted on some Galaxy devices. It works by replacing/patching runtime `res/raw` IMS configuration files so the device and system apps perceive compatible settings for VoWiFi, VoLTE, ViLTE, RCS and SMS over IP.

> This README documents device requirements, the exact manual activation workflow (pulling `imsservice.apk`, extracting the raw JSON files, editing them, compiling the module), credits, and legal/disclaimer notes.

---

## Features

- Replace runtime IMS configuration files via YukiHookAPI hooks.
- Restore VoWiFi / VoLTE / ViLTE / RCS / SMS over IP behavior on supported Samsung Galaxy devices.
- Minimal, transparent module that only replaces the four IMS raw files in `res/raw/`.

---

## Device requirements

Before using this module, ensure your device meets all of the following:

- **Root access** (Magisk).
- **LSPosed** installed and working as your host for runtime modules.
- A **Samsung Galaxy** device (**One UI 6 or newer**).
- The module **must be enabled** in LSPosed (see Installation section).

> If your device or software version is outside these constraints the module may not work and could cause instability.

---

## Files this module replaces

The module replaces the following JSON resources from the stock `imsservice.apk`:

- `res/raw/globalsettings.json`
- `res/raw/imsprofile.json`
- `res/raw/imsswitch.json`
- `res/raw/mnomap.json`

These files usually live inside the system `imsservice` APK supplied by Samsung. Replacing them at runtime allows IMS subsystems to read alternate operator/device-compatible values.

---

## Activation / Build workflow (step-by-step)

Follow these steps exactly. The instructions assume you have `adb` and a working Android build environment (Android Studio / Gradle) for the LSPosed module project.

### 0. Before Start
Make sure you have the ability to uninstall the app via TWRP recovery if something goes wrong with your device.

### 1. Locate and extract the stock `imsservice.apk` from your device

1. Connect your device via USB and enable `adb`.
2. Find the package path (example):

```bash
# show installed package path for imsservice
adb shell pm path com.sec.imsservice
# example output: package:/system/priv-app/imsservice/imsservice.apk
```

3. Pull the APK to your PC (adjust the path from previous command):

```bash
adb pull /system/priv-app/imsservice/imsservice.apk ./imsservice-stock.apk
```

> Note: Paths may vary by device / firmware. If `pm path` fails, search common locations such as `/system/priv-app/`, `/product/priv-app/` or use a root file explorer on the device.


### 2. Unpack the APK and extract the four `res/raw` files

Using `unzip` (quick):

```bash
unzip imsservice-stock.apk "res/raw/*" -d ./ims-raw-extracted
# then copy the four specific files
cp ims-raw-extracted/res/raw/globalsettings.json ./app/src/main/res/raw/
cp ims-raw-extracted/res/raw/imsprofile.json ./app/src/main/res/raw/
cp ims-raw-extracted/res/raw/imsswitch.json ./app/src/main/res/raw/
cp ims-raw-extracted/res/raw/mnomap.json ./app/src/main/res/raw/
```

### 3. Edit the extracted JSON files to match your operator

1. Open the files in a text editor and change the values necessary for your operator (PLMN, MCC/MNC, feature toggles, SIP/RCS endpoints, IMS profiles, etc.).
2. Validate JSON after editing (e.g. `jq . file.json` or an editor with JSON linting).

**Important:** Changing incorrect fields can break telephony/IMS behavior. Only change values you understand. If unsure, test incremental small edits rather than sweeping changes.


### 4. Place the edited files into this module project

Make sure the files exist at:

```
app/src/main/res/raw/globalsettings.json
app/src/main/res/raw/imsprofile.json
app/src/main/res/raw/imsswitch.json
app/src/main/res/raw/mnomap.json
```

If the `raw/` folder does not exist yet, create it.


### 5. Build the module

From the project root, build the module APK with Gradle (example):

```bash
# debug build
./gradlew assembleDebug
# or release
./gradlew assembleRelease
```

**OR USE ANDROID STUDIO**


### 6. Install and enable the module

1. Install the generated APK on the device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

2. Open **LSPosed Manager** on the device and enable the module for `com.sec.imsservice`.
3. Reboot the device for hooks to take effect.
4. Verify behavior (make a test VoWiFi/VoLTE call, check RCS availability, etc.).


---

## Security & Privacy

- This module edits runtime configuration used by system services. It does **not** exfiltrate user data intentionally. However, incorrect edits can change how the device communicates with operator or vendor services.
- Only use edited configuration files from sources you trust (preferably your own device's stock files).

---

## Disclaimer / Legal

This software is provided **as-is**. Use at your own risk. The author(s) hold no responsibility for damage, service loss, or warranty voiding caused by using this module. This project may affect emergency calling or carrier services — proceed with caution.

---

## Credits

Thanks to the projects and communities that made this work possible:

- [LSPosed Team](https://github.com/LSPosed)
- [YukiHookAPI](https://github.com/fankes/YukiHookAPI)


---


