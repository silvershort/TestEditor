package com.example.testeditor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImgStickerAdapter (val items: ArrayList<Int>) : RecyclerView.Adapter<ImgStickerAdapter.ImgStickerHolder>(), OnImgStickerClickListener {

    lateinit var listener: OnImgStickerClickListener

    class ImgStickerHolder(itemView: View, listener: OnImgStickerClickListener) : RecyclerView.ViewHolder(itemView) {

        val sticker_iv = itemView.findViewById<ImageView>(R.id.sticker_iv)

        init {
            itemView.setOnClickListener(View.OnClickListener {
                listener?.onStickerClick(this, adapterPosition)
            })
        }

        fun onBind(img: Int) {
            sticker_iv.setImageResource(img)
        }
    }

    public fun setOnImgStickerClickListener(listener: OnImgStickerClickListener) {
        this.listener = listener;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgStickerHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.sticker_img_item, parent, false)
        return ImgStickerHolder(view, this)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ImgStickerHolder, position: Int) {
        items[position].let {
            holder.onBind(it)
        }
    }

    override fun onStickerClick(holder: ImgStickerHolder, position: Int) {
        listener?.onStickerClick(holder, position)
    }

}