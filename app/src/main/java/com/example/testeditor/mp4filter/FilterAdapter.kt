package com.example.testeditor.mp4filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.testeditor.OnFilterClickListener
import com.example.testeditor.R

class FilterAdapter (val items : Array<FilterType>) : RecyclerView.Adapter<FilterAdapter.FilterHolder>(),
    OnFilterClickListener {

    lateinit var listener: OnFilterClickListener

    class FilterHolder(itemView: View, listener: OnFilterClickListener) : RecyclerView.ViewHolder(itemView) {
        val filter_card = itemView.findViewById<CardView>(R.id.filter_card)
        val filter_tv = itemView.findViewById<TextView>(R.id.filter_tv)

        init {
            itemView.setOnClickListener(View.OnClickListener {
                if (listener != null) {
                    listener.onFilterClick(this, adapterPosition)
                }
            })
        }

        fun onBind(filterName: String) {
            filter_tv.text = filterName
        }
    }

    public fun setOnFilterListener(listener: OnFilterClickListener) {
        this.listener = listener;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.filter_item, parent, false)
        return FilterHolder(
            view,
            this
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        items[position].let {
            holder.onBind(it.name)
        }
    }

    override fun onFilterClick(holder: FilterHolder, position: Int) {
        if (listener != null) {
            listener.onFilterClick(holder, position)
        }
    }

}