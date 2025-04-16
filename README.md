[![](https://jitpack.io/v/adnankanaan/KotlinFFmpegEditor.svg)](https://jitpack.io/#adnankanaan/KotlinFFmpegEditor)

# Kotlin Short Video Player & Editor Library 🎬

A simple and clean Android library to play & edit shorts videos using FFmpeg.

<p align="center">
  <img src="https://github.com/user-attachments/assets/582dfd25-e02c-4656-8ffa-cd6f74e93386" width="200"/>
  <img src="https://github.com/user-attachments/assets/a0653214-9115-4db6-ae71-e0cd8b26ea28" width="200"/>
  <img src="https://github.com/user-attachments/assets/571e6e15-dde7-4a61-986c-290eb61d736c" width="200"/>
  <img src="https://github.com/user-attachments/assets/4d08719a-3b30-4486-9ab8-67a9b19d95e5" width="200"/>
</p>

* Check out the original version of video player & editor activity on google play at  : https://play.google.com/store/apps/details?id=com.smart.loader

## Features
- Smoth & Responsive video player for all screen layout
- Up to 5 min video length for generate process
- High quality generated edit video
- Free draw on videos with color picker
- imoji overlay
- Add text overlay with color picker
- Face swap detection
- Draggable & resize responsive canvas
- Trim video (Soon in next release 1.1.0)
- FFmpeg processing core
- Easy to integrate with one line
## 📦 Installation

Add JitPack to your root `settings.gradle` or `build.gradle`:

```groovy
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
```groovy
dependencies {
    implementation 'com.github.adnankanaan:KotlinFFmpegEditor:v1.0.9'
}

//or

dependencies {
    implementation 'com.github.adnankanaan:KotlinFFmpegEditor:1.0.9'
}
```
## Usage
just we need to pass the selected video file path by user to the editer activity :
```kotlin
// Current video file path
var videoPath = videoFile.absolutePath // for example existing local video file absolutePath

// Start video editer activity
Intent(requireContext(), VideoEditorActivity::class.java).apply {
   putExtra("videoPath", videoPath.toString()) // pass the video absolutePath here
   startActivity(this)
}
```
## Optinal Customize
befor you intent VideoEditorActivity you can Customize the default text, color and the current lang by adding this line :
```kotlin
VideoEditorFragment.textFinalSet = "Hello!" // default text input
VideoEditorFragment.textFinalColorSet = "#FF0000" // default text color
VideoEditorFragment.currentLang = "ar" // "ar" for arabic or "en" for english activity layout
```
## Permissions & Activity declare Needed (for host app)

Make sure to add the following permissions & Activity declare tag in your app's `AndroidManifest.xml`:

```xml
<!-- Require permissions tag -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="32" />

<!-- inside your application block add the required video editor activity declarment tag -->
<application ......
      .......>
      <!-- Start-video-editer-activity-tag -->

        <activity
            android:name="com.webapp.kotlin_webapp_video_editer.videoeditor.VideoEditorActivity"
            android:parentActivityName=".MainActivity" // Change to your main activity if it is not MainActivity or any previous parent activity logic, Or you can remove it safly
            android:screenOrientation="portrait"
            android:exported="true"
            tools:ignore="LockedOrientationActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>

      <!-- End-video-editer-activity-tag -->
   .........
</application>
```
## Notes
* the editor button visible only when the selected input video less than 5 minutes > Editing video only for short videos.
* This is first release version and may need more improvements .
* minSdk 24
* targetSdk 35

