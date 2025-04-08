# Kotlin Short Video Editor Library 🎬

A simple and clean Android library to edit videos using FFmpeg.

<p align="center">
  <img src="https://github.com/user-attachments/assets/582dfd25-e02c-4656-8ffa-cd6f74e93386" width="200"/>
  <img src="https://github.com/user-attachments/assets/a0653214-9115-4db6-ae71-e0cd8b26ea28" width="200"/>
  <img src="https://github.com/user-attachments/assets/571e6e15-dde7-4a61-986c-290eb61d736c" width="200"/>
  <img src="https://github.com/user-attachments/assets/4d08719a-3b30-4486-9ab8-67a9b19d95e5" width="200"/>
</p>

try out the orgenal ver of video editer on google play at : https://play.google.com/store/apps/details?id=com.smart.loader

## Features
- Free draw on videos with color picker
- imoji overlay
- Add text overlay with color picker
- Face swap detection
- Draggable & resize responsive canvas
- Easy to integrate one line to video start editer activty

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


Then add this to your module's build.gradle:

dependencies {
    implementation 'com.github.adnankanaan:KotlinFFmpegEditor:1.0.0'
}

## Usage
just we need to pass the selected videoUri by user to the editer activty , as this :

var videoUri = videoFile.absolutePath // for example existing local video file

val intent = Intent(context, VideoEditorActivity::class.java)
intent.putExtra("videoUri", videoUri) // pass the videoUri here
startActivity(intent)

## Optinal Customize
befor you intent VideoEditorActivity you can Customize the default text, color and the current lang by adding this line :

VideoEditorFragment.textFinalSet = "Hello!"
VideoEditorFragment.textFinalColorSet = "#FF0000"
VideoEditorFragment.currentLang = "ar" // "ar" for arabic or "en" for english

## Notes
* the editor button visible only when input video less than 5 minutes > only shorts video.
* This is first trial and colud need more improvments .

