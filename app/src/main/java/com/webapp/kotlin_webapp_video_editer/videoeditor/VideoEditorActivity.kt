package com.webapp.kotlin_webapp_video_editer.videoeditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

class VideoEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(android.R.id.content, VideoEditorFragment())
            }
        }
    }
}

