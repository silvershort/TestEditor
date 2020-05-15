package com.example.testeditor

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AudioSelectAdapter (val items: ArrayList<AudioData>) : RecyclerView.Adapter<AudioSelectAdapter.AudioHolder>(),
                OnAudioSelectClickListener{

    private val TAG = "!!!AudioSelectAdapter!!!"

    lateinit var listener: OnAudioSelectClickListener

    class AudioHolder(itemView: View, listener: OnAudioSelectClickListener) : RecyclerView.ViewHolder(itemView) {

        val audio_tv_title = itemView.findViewById<TextView>(R.id.audio_tv_title)
        val audio_ib_play = itemView.findViewById<ImageButton>(R.id.audio_ib_play)
        val audio_ib_done = itemView.findViewById<ImageButton>(R.id.audio_ib_done)

        init {
            audio_ib_play.setOnClickListener(View.OnClickListener {
                listener?.playButtonClick(this, adapterPosition)
            })
            audio_ib_done.setOnClickListener(View.OnClickListener {
                listener?.selectButtonClick(this, adapterPosition)
            })
        }

        fun onBind(item: AudioData) {
            audio_tv_title.text = item.title
        }
    }

    fun setOnAudioSelectClickListener(listener: OnAudioSelectClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.audio_item, parent, false)
        return AudioHolder(view, this)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AudioHolder, position: Int) {
        items[position].let {
            Log.d(TAG, it.url)
            holder.onBind(it)
        }
    }

    override fun playButtonClick(holder: AudioHolder, position: Int) {
        listener?.playButtonClick(holder, position)
    }

    override fun selectButtonClick(holder: AudioHolder, position: Int) {
        listener?.selectButtonClick(holder, position)
    }

}