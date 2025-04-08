# Kotlin Video Editor Library 🎬

A simple and clean Android library to edit videos using FFmpeg.

## Features
- Trim videos
- Add text overlay
- Face swap (coming soon!)
- Easy to integrate

## Usage

```kotlin
val intent = Intent(context, VideoEditorActivity::class.java)
intent.putExtra("videoUri", videoUri)
startActivity(intent)

## Customize

VideoEditorFragment.textFinalSet = "Hello!"
VideoEditorFragment.textFinalColorSet = "#FF0000"
VideoEditorFragment.currentLang = "ar"

