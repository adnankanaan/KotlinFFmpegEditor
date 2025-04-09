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

class FaceSwapDialog : DialogFragment() {

    private lateinit var faceAdapter: FaceAdapter
    private var onFaceSelected: ((String) -> Unit)? = null

    fun setOnFaceSelectedListener(listener: (String) -> Unit) {
        onFaceSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(ContextThemeWrapper(requireContext(), R.style.AlertDialogCustom))
        val resourcesSet = ChangeLangBase().setLocale(requireContext(), UserData.current_lang)!!.resources

        builder.setTitle(resourcesSet.getString(R.string.select_face_title))
        // RecyclerView to display emojis
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager =  GridLayoutManager(requireContext(), 4)
            faceAdapter = FaceAdapter { faceName ->
                onFaceSelected?.invoke(faceName)
                dismiss() // Close the dialog when an emoji is selected
            }
            adapter = faceAdapter
        }

        // Fetch emoji list from API and update adapter
        fetchFaces { faceNames ->
            faceAdapter.setFaces(faceNames)
        }

        builder.setView(recyclerView)
        return builder.create()
    }

    private fun fetchFaces(callback: (List<String>) -> Unit) {
        // Fetch emojis from API in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${getString(R.string.editer_kotlin_lib_ss8_api_url)}get-faces-list")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                responseBody?.let {
                    val faceList = mutableListOf<String>()
                    val jsonObject = JSONObject(it)
                    val jsonArray = jsonObject.getJSONArray("face_list")

                    for (i in 0 until jsonArray.length()) {
                        val faces = jsonArray.getJSONObject(i)
                        val faceName = faces.getString("name")
                        faceList.add(faceName)
                    }

                    // Switch to Main thread to update UI
                    withContext(Dispatchers.Main) {
                        callback(faceList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
