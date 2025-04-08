# Kotlin Short Video Editor Library 🎬

A simple and clean Android library to edit videos using FFmpeg.

![Screenshot_20250408_123222_Smart Loader](https://github.com/user-attachments/assets/582dfd25-e02c-4656-8ffa-cd6f74e93386)
![Screenshot_20250408_122708_Smart Loader](https://github.com/user-attachments/assets/a0653214-9115-4db6-ae71-e0cd8b26ea28)
![Screenshot_2025040![Uploading Screenshot_20250408_122457_Smart Loader.jpg…]()
8_122527_Smart Loader](https://github.com/user-attachments/assets/571e6e15-dde7-4a61-986c-290eb61d736c)


## Features
- Free draw on videos
- imoji overlay
- Add text overlay
- Face swap detection 
- Easy to integrate

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

val intent = Intent(context, VideoEditorActivity::class.java)
intent.putExtra("videoUri", videoUri)
startActivity(intent)

## Customize

VideoEditorFragment.textFinalSet = "Hello!"
VideoEditorFragment.textFinalColorSet = "#FF0000"
VideoEditorFragment.currentLang = "ar"

## Notes
* the editor button visible only when input video less than 5 minutes > only shorts video.
* This is first trial and colud need more improvments .

