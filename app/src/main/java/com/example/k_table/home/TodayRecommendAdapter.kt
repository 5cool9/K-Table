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
    private val title: String,
    private val description: String
) : RecyclerView.Adapter<TodayRecommendAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgSlide: ImageView =
            view.findViewById(R.id.imgSlide)

        val tvIndicator: TextView =
            view.findViewById(R.id.tvPageIndicator)

        val tvTitle: TextView =
            view.findViewById(R.id.tvSlideTitle)

        val tvDesc: TextView =
            view.findViewById(R.id.tvSlideDesc)
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

        holder.tvTitle.text =
            title

        holder.tvDesc.text =
            description
    }


    override fun getItemCount(): Int {
        return images.size
    }
}