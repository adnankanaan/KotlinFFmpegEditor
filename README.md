# Kotlin Short Video Editor Library 🎬

A simple and clean Android library to edit videos using FFmpeg.

<p align="center">
  <img src="https://github.com/user-attachments/assets/582dfd25-e02c-4656-8ffa-cd6f74e93386" width="200"/>
  <img src="https://github.com/user-attachments/assets/a0653214-9115-4db6-ae71-e0cd8b26ea28" width="200"/>
  <img src="https://github.com/user-attachments/assets/571e6e15-dde7-4a61-986c-290eb61d736c" width="200"/>
  <img src="https://github.com/user-attachments/assets/4d08719a-3b30-4486-9ab8-67a9b19d95e5" width="200"/>
</p>

* Try out the orgenal ver of video editer activity on google play at : https://play.google.com/store/apps/details?id=com.smart.loader

## Features
- Free draw on videos with color picker
- imoji overlay
- Add text overlay with color picker
- Face swap detection
- Draggable & resize responsive canvas
- Easy to integrate one line to video start editer activty
## 📦 Installation

Add JitPack to your root `settings.gradle` or `build.gradle`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

```
Then add the lib to your module's build.gradle:
```kotlin
dependencies {
    implementation 'com.github.adnankanaan:KotlinFFmpegEditor:1.0.7'
}
```
## Usage
just we need to pass the selected videoUri by user to the editer activty , as this :
```
var videoPath = videoFile.absolutePath // for example existing local video file

val intent = Intent(context, VideoEditorActivity::class.java)
intent.putExtra("videoPath", videoPath.toString()) // pass the videoUri here
startActivity(intent)
```
## Optinal Customize
befor you intent VideoEditorActivity you can Customize the default text, color and the current lang by adding this line :
```kotlin
VideoEditorFragment.textFinalSet = "Hello!"
VideoEditorFragment.textFinalColorSet = "#FF0000"
VideoEditorFragment.currentLang = "ar" // "ar" for arabic or "en" for english
```
## Permissions Needed (for host app)

Make sure to add the following permissions in your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="32" />

// inside application block add the editer activity declear
<application .......
        <activity
            android:name="com.webapp.kotlin_webapp_video_editer.videoeditor.VideoEditorActivity"
            android:parentActivityName=".MainActivity"
            android:screenOrientation="portrait"
            android:exported="true"
            tools:ignore="LockedOrientationActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
</application>
```
## Notes
* the editor button visible only when input video less than 5 minutes > only shorts video.
* This is first trial and may need more improvements .

