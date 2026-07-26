package com.example.k_table

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.k_table.model.Restaurant

class RestaurantAdapter(private val items: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    private val tagImageMap = mapOf(
        "HALAL" to R.drawable.tag_halal,
        "PORK_FREE" to R.drawable.tag_pork_free,
        "VEGAN" to R.drawable.tag_vegan
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRestaurantName)
        val tvAddress: TextView = view.findViewById(R.id.tvRestaurantLocation)
        val tvFeature: TextView = view.findViewById(R.id.tvRestaurantDesc)
        val imgRestaurant: ImageView = view.findViewById(R.id.imgRestaurant)
        val layoutTags: LinearLayout = view.findViewById(R.id.layoutTags)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvAddress.text = item.address
        holder.tvFeature.text = item.feature

        // 사진은 자체 기본 이미지 사용 (카카오 API가 사진을 제공하지 않음)
        holder.imgRestaurant.setImageResource(R.drawable.none_place)

        // 태그 동적으로 그리기
        holder.layoutTags.removeAllViews()

        for (tagCode in item.tags) {

            val imageRes = tagImageMap[tagCode] ?: continue

            val tagImage = ImageView(holder.itemView.context).apply {

                setImageResource(imageRes)

                layoutParams = LinearLayout.LayoutParams(
                    70.dpToPx(holder.itemView.context),
                    25.dpToPx(holder.itemView.context)
                ).apply {
                    marginEnd = 8.dpToPx(holder.itemView.context)
                }

                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            holder.layoutTags.addView(tagImage)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}