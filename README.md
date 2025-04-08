# Kotlin Video Editor Library 🎬

A simple and clean Android library to edit videos using FFmpeg.

## Features
- Trim videos
- Add text overlay
- Face swap (coming soon!)
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

groovy
Copy
Edit
dependencies {
    implementation 'com.github.YOUR_USERNAME:YOUR_REPO_NAME:VERSION_TAG'
}


## Usage

```kotlin
val intent = Intent(context, VideoEditorActivity::class.java)
intent.putExtra("videoUri", videoUri)
startActivity(intent)

## Customize

VideoEditorFragment.textFinalSet = "Hello!"
VideoEditorFragment.textFinalColorSet = "#FF0000"
VideoEditorFragment.currentLang = "ar"

