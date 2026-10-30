# Phone Audio – control your phone's audio from a Garmin fēnix 6X

Control whatever is playing on your Android phone (Spotify, YouTube, podcasts, …) straight from your Garmin watch: play/pause, next/previous track and volume.

The project has two parts that talk to each other through Garmin Connect:

| Folder     | What it is                                                                                  |
|------------|---------------------------------------------------------------------------------------------|
| `watch/`   | Connect IQ watch app written in Monkey C, targeting the **fēnix 6X Pro**                    |
| `android/` | Android companion app (Kotlin) that receives commands and drives the active media session   |
| `dist/`    | (not tracked) prebuilt binaries are published under GitHub Releases                          |

## How it works

1. The watch app sends a small message (`playpause`, `next`, `prev`, `volup`, `voldown`, `sync`) to the phone through the Connect IQ `Communications` API.
2. The Android app (`BridgeService`, a foreground service) receives it via the Connect IQ Companion App SDK.
3. The command is forwarded to the currently active media session. Android requires the *notification access* permission to control other apps' players, which is why the app asks for it.

## Controls on the watch

- **START**: play / pause
- **UP / DOWN**: next / previous track (TRACKS mode)
- **Long-press UP**: switch to VOLUME mode, where UP / DOWN = volume + / −

## Installation

### Android
1. Download `PhoneAudioBridge.apk` from the [Releases](../../releases) page and install it (you may need to allow installs from unknown sources).
2. Open the app and grant **notification access** (needed to control other apps' media players).
3. Tap **Start the bridge** and exclude the app from battery optimization so Android doesn't kill it.

### Watch
1. Connect the watch via USB and copy `PhoneAudio.prg` (from the [Releases](../../releases) page) to `GARMIN/APPS/`.
2. Garmin Connect must be installed on the phone with the watch paired.

## Building from source

Requirements: JDK 21, [Connect IQ SDK](https://developer.garmin.com/connect-iq/sdk/), Android SDK 34.

**Watch app**

```sh
monkeyc -d fenix6xpro -f watch/monkey.jungle -o build/PhoneAudio.prg -y keys/developer_key.der
```

You need your own Connect IQ developer key. Generate one following the
[Garmin docs](https://developer.garmin.com/connect-iq/connect-iq-basics/getting-started/) and put it in `keys/` (this folder is git-ignored – never commit or share your key).

**Android app**

```sh
cd android
./gradlew assembleDebug
```

Create `android/local.properties` with your SDK path (`sdk.dir=/path/to/Android/sdk`); the file is git-ignored.

## Project layout

```
watch/source/     Monkey C sources (app, view, delegate, model, phone link)
watch/resources/  strings and launcher icon
android/app/      Kotlin sources: MainActivity, BridgeService, MediaController, MediaAccessService, BootReceiver
```

## Status

Personal project, tested on a fēnix 6X Pro with Android. Other Garmin models would need to be added to `watch/manifest.xml`.

## License

MIT – see [LICENSE](LICENSE).
