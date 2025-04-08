# Kotlin Short Video Editor Library 🎬

A simple and clean Android library to edit videos using FFmpeg.

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
* the editor button visible only when input video less than 5 minutes > only shorts video

