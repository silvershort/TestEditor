package com.example.testeditor.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testeditor.AudioData
import com.example.testeditor.AudioSelectAdapter
import com.example.testeditor.OnAudioSelectClickListener
import com.example.testeditor.R
import com.example.testeditor.api.APIClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class AudioSelectDialog : DialogFragment() {

    private val TAG = "!!!AudioSelectDialog!!!"

    private lateinit var dialogResult: OnDialogResult
    private lateinit var audio_recyclerview : RecyclerView

    private val audioList = arrayListOf<AudioData>(
        AudioData("News_Room_News", "photo/2020/01/29/20/24/architecture-4803602_960_720.jpg"),
        AudioData("I_Found_DB_Cooper", "music/royalty-free/mp3-royaltyfree/A%20Very%20Brady%20Special.mp3"),
        AudioData("The_Old_RV", "music/royalty-free/mp3-royaltyfree/Frogs%20Legs%20Rag.mp3")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.audio_select_dialog, container)

        dialog?.setCanceledOnTouchOutside(false)

        audio_recyclerview = view.findViewById(R.id.audio_recyclerview)

        val audioSelectAdapter = AudioSelectAdapter(audioList)
        audio_recyclerview.adapter = audioSelectAdapter
        audio_recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        audioSelectAdapter.setOnAudioSelectClickListener(object : OnAudioSelectClickListener{
            override fun playButtonClick(holder: AudioSelectAdapter.AudioHolder, position: Int) {

            }

            override fun selectButtonClick(holder: AudioSelectAdapter.AudioHolder, position: Int) {
                APIClient.getAPIInterface().getAudioDownload(audioList[position].url).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Log.d(TAG, "onFailure : ${t.toString()}")
                        }
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            Log.d(TAG, "onResponse : ${response.toString()}")
                            writeResponseBodyToDisk(response.body()!!, position)
                        }
                    }
                )
            }
        })

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!, theme) {
            override fun onBackPressed() {
                Log.d(TAG, "백버튼이 다이얼로그에서 눌림")
                dismiss()
            }
        }
    }

    fun writeResponseBodyToDisk(body: ResponseBody, position: Int): Boolean {
        val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST/"
        val directory = File(rootPath)

        if (!directory.exists()) {
            Log.d(TAG, "path: $directory")
            Log.d(TAG, "folder create")
            directory.mkdir()
        }

        val file = File(rootPath, audioList[position].title + ".mp3")

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        val fileReader = ByteArray(4096)

        val fileSize = body.contentLength()
        var fileSizeDownloaded = 0;

        inputStream = body.byteStream()
        outputStream = FileOutputStream(file)

        while(true) {
            val read = inputStream.read(fileReader)

            if (read == -1) {
                break
            }

            outputStream.write(fileReader, 0, read)
            fileSizeDownloaded += read
            Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
        }

        outputStream.flush()

        outputStream.close()
        inputStream.close()

        return true
    }

    fun setDialogResultInterface(dialogResult: OnDialogResult) {
        this.dialogResult = dialogResult
    }

    interface OnDialogResult {
        fun select(path: String)
    }

}