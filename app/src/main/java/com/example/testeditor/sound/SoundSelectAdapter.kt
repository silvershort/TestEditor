package com.example.testeditor.sound

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testeditor.R

class SoundSelectAdapter (val items: ArrayList<SoundData>) : RecyclerView.Adapter<SoundSelectAdapter.SoundHolder>(),
    OnSoundSelectClickListener {

    private val TAG = "!!!AudioSelectAdapter!!!"

    lateinit var listener: OnSoundSelectClickListener

    class SoundHolder(itemView: View, listener: OnSoundSelectClickListener) : RecyclerView.ViewHolder(itemView) {

        val audio_tv_title = itemView.findViewById<TextView>(R.id.audio_tv_title)
        val audio_ib_play = itemView.findViewById<ImageButton>(R.id.audio_ib_play)
        val audio_ib_done = itemView.findViewById<ImageButton>(R.id.audio_ib_done)

        init {
            audio_tv_title.isSingleLine = true
            audio_tv_title.ellipsize = TextUtils.TruncateAt.MARQUEE
            audio_tv_title.isSelected = true

            audio_ib_play.setOnClickListener(View.OnClickListener {
                listener?.playButtonClick(this, adapterPosition)
            })
            audio_ib_done.setOnClickListener(View.OnClickListener {
                listener?.selectButtonClick(this, adapterPosition)
            })
        }

        fun onBind(item: SoundData) {
            audio_tv_title.text = item.title
        }
    }

    fun setOnSoundSelectClickListener(listener: OnSoundSelectClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.audio_item, parent, false)
        return SoundHolder(
            view,
            this
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SoundHolder, position: Int) {
        items[position].let {
            Log.d(TAG, it.url)
            holder.onBind(it)
        }
    }

    override fun playButtonClick(holder: SoundHolder, position: Int) {
        listener?.playButtonClick(holder, position)
    }

    override fun selectButtonClick(holder: SoundHolder, position: Int) {
        listener?.selectButtonClick(holder, position)
    }

}