package com.example.k_table.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.k_table.R

class TodayRecommendAdapter(
    private val images: List<Int>,
) : RecyclerView.Adapter<TodayRecommendAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgSlide: ImageView =
            view.findViewById(R.id.imgSlide)

        val tvIndicator: TextView =
            view.findViewById(R.id.tvPageIndicator)

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_today_recommend,
                parent,
                false
            )

        return ViewHolder(view)
    }


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.imgSlide.setImageResource(
            images[position]
        )

        holder.tvIndicator.text =
            "${position + 1}/${images.size}"

    }


    override fun getItemCount(): Int {
        return images.size
    }
}