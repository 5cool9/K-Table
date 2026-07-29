package com.example.k_table.scan
import android.animation.ObjectAnimator
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.k_table.R
import com.example.k_table.databinding.ItemScanResultBinding
import com.example.k_table.model.MenuScanResult
import com.example.k_table.model.SuitabilityStatus
import android.content.Context

class ScanResultAdapter(
    private val onListenClick: (String?) -> Unit
) : ListAdapter<MenuScanResult, ScanResultAdapter.ScanResultViewHolder>(DiffCallback) {

    // 현재 펼쳐진 아이템의 id 를 저장
    private var expandedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
        val binding = ItemScanResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScanResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id == expandedId)
    }

    inner class ScanResultViewHolder(
        private val binding: ItemScanResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuScanResult, isExpanded: Boolean) {
            binding.tvKoreanName.text = item.koreanName
            binding.tvEnglishName.text = item.englishName

            binding.tvPrice.text =
                if (!item.price.isNullOrEmpty()) {
                    "${item.price}원"
                } else {
                    ""
                }


            val colorRes = when (item.status) {
                SuitabilityStatus.SUITABLE -> R.color.status_green
                SuitabilityStatus.CAUTION -> R.color.status_orange
                SuitabilityStatus.UNSUITABLE -> R.color.status_red
            }
            binding.statusDot.setColorFilter(
                binding.root.context.getColor(colorRes)
            )

            // 확장 영역 표시 여부
            binding.ivChevron.visibility =
                if (item.status == SuitabilityStatus.UNSUITABLE)
                    View.GONE
                else
                    View.VISIBLE
            binding.expandableContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivChevron.rotation = if (isExpanded) 180f else 0f

            // 질문 텍스트 세팅
            binding.tvQuestion.text = item.questionKo ?: ""

            // 기본값 복구
            binding.ivChevron.visibility = View.VISIBLE

            // 질문이 없으면 확장 불가 처리
            if (item.questionKo.isNullOrEmpty()) {
                binding.expandableContent.visibility = View.GONE
                binding.ivChevron.visibility = View.GONE
            } else {
                binding.ivChevron.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {

                // 부적합은 질문 영역 없으니까 열리지 않도록
                if (item.status == SuitabilityStatus.UNSUITABLE) {
                    return@setOnClickListener
                }

                val previousExpandedId = expandedId
                expandedId = if (isExpanded) null else item.id

                notifyItemChanged(bindingAdapterPosition)

                if (previousExpandedId != null && previousExpandedId != item.id) {
                    val prevPos = currentList.indexOfFirst { it.id == previousExpandedId }
                    if (prevPos != -1) notifyItemChanged(prevPos)
                }
            }

            // 화살표 회전 애니메이션
            binding.ivChevron.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(150)
                .start()

            binding.btnListen.visibility =
                if (item.questionKo.isNullOrEmpty())
                    View.GONE
                else
                    View.VISIBLE

            binding.btnListen.setOnClickListener {
                onListenClick(item.questionKo)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<MenuScanResult>() {
        override fun areItemsTheSame(oldItem: MenuScanResult, newItem: MenuScanResult) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MenuScanResult, newItem: MenuScanResult) =
            oldItem == newItem
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}