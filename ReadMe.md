# Temo - Text Screen and Morse Display


<a href="https://play.google.com/store/apps/details?id=com.nndwn.runtext">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" width="240"/>
</a>

Temo is an app for creating running and blinking text that can be fully customized with various styles. It can be displayed on screen, and the animated text can be shared to social media in MP4 format. Morse code is included as an additional feature.

## Common
* **Preview** Displays the configured text or Morse code.
* **Input Text** Text and Morse input is limited to 40 characters.
* **Share and Download** Configured output can be downloaded and shared to social media.

### Running Text
<p align="center">
  <img src="/raw/sc1.png" alt="running text" width="895">
</p>

An animation that can run on the phone screen, and the resulting text design can be shared to social media in MP4 format. The text itself is limited to 40 characters, including spaces.
* **Aware Text Direction** Sometimes text can be written from left-to-right and right-to-left; the running text will adjust accordingly.
* **Presets** A collection of pre-configured settings, so you don't have to worry about creating text designs—just use what's already available.
* **Speed** Text speed settings with a maximum of 500 px/s and a minimum of 50 px/s.
* **Animation Text** Running animations like text moving from left to right or right to left for text starting from the right. Additionally, it provides a text blinking animation if blink mode is enabled.
* **Mirror Mode** This feature is useful if the text is viewed through a mirror reflection so it can be read properly.
* **Font Style** Settings for various font styles and text shapes.
    * **Variant Font** There are 57 fonts available. Fonts are either bundled in the app or downloaded on demand per script from a self-hosted bundle. The full list is in [fonts.json](app/src/main/res/raw/fonts.json), and the downloadable bundles are defined in [bundles.json](app/src/main/res/raw/bundles.json).
    * **Script Support** Fonts support various writing systems and scripts, such as Latin, Arabic, Japanese, Chinese, Korean, Thai, Devanagari, Khmer, and Hebrew.
* **Background** Screen background that can be customized with solid colors.
* **Text Color** Text color provides 2 choices: solid color and gradient.
    * **Solid Color** Text color can be customized with solid colors.
    * **Gradient Color** Text color can be customized with gradient colors, including adjustable gradient length and orientation (vertical or horizontal).
* **Outline** Customizes the outline border of the text:
  * **Outline Color** Customizes solid outline color with transparency support.
  * **Outline Width** Customizes outline width (minimum 1, maximum 10).
* **Drop Shadow** Customizes the shadow of the text:
  * **Color** Customizes solid shadow color with transparency support.
  * **Radius** Customizes the blur radius of the text shadow.
  * **Rotation** Customizes the rotation position of the shadow.

### Morse
---
<p align="center">
  <img src="/raw/sc5.png" alt="morse code" width="895">
</p>

Morse mode converts your typed text into International Morse Code and transmits it as light, sound, and vibration signals. The screen flashes to represent dots (`.`), dashes (`-`), and the gaps in between. Just like Running Text, the input is limited to 40 characters (spaces included) and the signal can be downloaded and shared to social media as an MP4 video.

* **Instant Conversion** Every character you type is automatically translated into Morse code using the International Morse Code standard, with correct timing for intra-character gaps (between dots/dashes of the same letter), inter-character gaps (between letters), and inter-word gaps (between words). No manual `.`/`-` typing needed.
* **SOS Shortcut** Typing `SOS` automatically plays the universal distress pattern `... --- ...` continuously, without you having to build it manually.
* **Speed (WPM)** Adjusts how fast the Morse code is transmitted, measured in Words Per Minute (WPM). The range is 5 WPM (slow and easy to follow) up to 40 WPM (fast), with a default of 15 WPM. Timing follows the PARIS standard, where 1 unit = `1200 / WPM` milliseconds.
* **Screen Flash** When enabled, the screen flashes in a bright color in sync with the Morse signal; when disabled, the screen stays black (dark). At least one output (Screen Flash or Flashlight) must be enabled, otherwise the app shows a notice asking you to turn one on so the Morse code can be displayed.
* **Morse Color** Customizes the flash color used for the signal (the background color that lights up for dots and dashes). This makes the signal more visible or easier on the eyes depending on the environment.
* **Flashlight / Torch** Uses the phone's rear camera flash (LED) to transmit the Morse signal, which is useful for longer distances or in complete darkness. This requires the Camera permission; if it is denied, the app shows a notice with instructions. When on, the torch blinks on for signals and off for gaps.
* **Sound** Plays an audible tone/beep for every dot and dash, so the Morse can be listened to as well as seen. The beep duration matches the signal duration.
* **Vibration** Vibrates the phone in sync with each signal pulse (dots and dashes), which is handy for silent/haptic-only use or when the screen cannot be watched.
* **Korean Hangul Support** In addition to standard Latin text, Hangul characters are converted to Morse code using the SKATS (Standard Korean Alphabet Transliteration System) mapping, including support for composite Jamo.

## Installation

### Option 1 — Install from Google Play

<a href="https://play.google.com/store/apps/details?id=com.nndwn.runtext">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" width="240"/>
</a>

Requires an Android device running **Android 9 (API 28)** or newer.

### Option 2 — Build and run from source

#### Prerequisites
* **Android Studio** (latest stable), or the Android SDK command-line tools.
* **JDK 17** (Android Studio ships with a compatible JDK).
* **Android SDK** with Platform/Build-Tools matching `compileSdk = 37`.
* A **device or emulator** running **Android 9 (API 28)** or newer.
* (Optional) A device with a **rear camera flash** to test the Flashlight / Torch output.

#### Steps
1. **Clone the repository**
   ```shell
   git clone https://github.com/nndwn/temo.git
   cd temo
   ```

2. **Open the project** in Android Studio and let Gradle sync, or use the Gradle wrapper from the CLI (Gradle is pinned to `9.7.0` via the wrapper, so no local install is needed).

3. **(Optional) Configure release signing.** Building and running in debug mode works out of the box (the app falls back to the debug signing config). To build a signed release, copy `local.properties.default` to `local.properties` and fill in the keystore values:
   ```properties
   KEY_FILE=
   KEYSTORE_PASSWORD=
   KEY_ALIAS=
   KEY_PASSWORD=
   ```

4. **Build and/or install** using one of the product flavors:
   * Linux / macOS:
     ```shell
     ./gradlew assembleFossDebug      # build the APK
     ./gradlew installFossDebug       # build and install on a connected device
     ```
   * Windows:
     ```shell
     gradlew.bat assembleFossDebug
     gradlew.bat installFossDebug
     ```

5. **Or simply press ▶ Run** in Android Studio using the `app` run configuration.

#### Product flavors

| Flavor      | Description                                                                             | Example tasks                            |
|-------------|-----------------------------------------------------------------------------------------|------------------------------------------|
| `playstore` | Distribution via Google Play; includes in-app update, billing, and in-app review.        | `assemblePlaystoreDebug`, `installPlaystoreDebug` |
| `foss`      | FOSS build (no Play Services features); the version name gets a `-foss` suffix.          | `assembleFossDebug`, `installFossDebug`  |

#### Tests

```shell
./gradlew testPlaystoreDebugUnitTest testFossDebugUnitTest
```

#### Useful commands

```shell
# Download missing fonts and generate:
#   - build/downloaded-fonts/*.ttf      (raw downloaded fonts)
#   - build/font-bundles/<script>.zip   (per-script bundles, with SHA-256)
#   - app/src/main/res/raw/bundles.json (bundle manifest: version + URLs + hashes)
#   - app/src/main/res/raw/fonts.json   (font metadata)
./gradlew :getgooglefont-compressit:compressFonts \
  -PbundleVersion=fonts-2026.01 \
  -PbaseUrl=https://github.com/<owner>/temo/releases/download

# Optional flags for the compressFonts task:
#   -Pzip                 also create the combined raw/compressed_fonts.zip
#   -Plzma                also create raw/compressed_fonts.7z (LZMA2, smaller but heavier)
#   -PbundleVersion=...   tag used in the bundle download URL (default: fonts-1)
#   -PbaseUrl=...         base URL prefix for bundle download links

# Reset the app data on a connected device
adb shell pm clear com.nndwn.runtext

# Publish to Google Play (Triple-T Gradle Play Publisher, requires play-service-account.json)
./gradlew publishPlaystoreReleaseBundle
```

## License

Released under the [MIT License](LICENSE).
