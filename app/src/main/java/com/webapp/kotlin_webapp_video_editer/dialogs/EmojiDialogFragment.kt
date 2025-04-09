package com.webapp.kotlin_webapp_video_editer.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.webapp.kotlin_webapp_video_editer.R
import com.webapp.kotlin_webapp_video_editer.videoeditor.ChangeLangBase
import com.webapp.kotlin_webapp_video_editer.videoeditor.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class EmojiDialogFragment : DialogFragment() {

    private lateinit var emojiAdapter: EmojiAdapter
    private var onEmojiSelected: ((String) -> Unit)? = null

    fun setOnEmojiSelectedListener(listener: (String) -> Unit) {
        onEmojiSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(ContextThemeWrapper(requireContext(), R.style.AlertDialogCustom))
        val resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources

        builder.setTitle(resourcesSet.getString(R.string.select_emoji_title))

        // RecyclerView to display emojis
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager =  GridLayoutManager(requireContext(), 4)
            emojiAdapter = EmojiAdapter { emojiUrl ->
                onEmojiSelected?.invoke(emojiUrl)
                dismiss() // Close the dialog when an emoji is selected
            }
            adapter = emojiAdapter
        }

        // Fetch emoji list from API and update adapter
        fetchEmojis { emojiUrls ->
            emojiAdapter.setEmojis(emojiUrls)
        }

        builder.setView(recyclerView)
        return builder.create()
    }

    private fun fetchEmojis(callback: (List<String>) -> Unit) {
        // Fetch emojis from API in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${getString(R.string.editer_kotlin_lib_ss8_api_url)}get-emoji-list")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                responseBody?.let {
                    val emojiList = mutableListOf<String>()
                    val jsonObject = JSONObject(it)
                    val jsonArray = jsonObject.getJSONArray("imoji_list")

                    for (i in 0 until jsonArray.length()) {
                        val emoji = jsonArray.getJSONObject(i)
                        val emojiUrl = emoji.getString("imojy_url")
                        emojiList.add(emojiUrl)
                    }

                    // Switch to Main thread to update UI
                    withContext(Dispatchers.Main) {
                        callback(emojiList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
