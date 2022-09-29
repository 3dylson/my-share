package pt.ms.myshare.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.ms.myshare.data.InvestAmount
import pt.ms.myshare.databinding.GridViewItemBinding

class CategoryGridAdapter :
    ListAdapter<InvestAmount, CategoryGridAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<InvestAmount>() {
        override fun areItemsTheSame(oldItem: InvestAmount, newItem: InvestAmount): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: InvestAmount, newItem: InvestAmount): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(private var binding: GridViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(investAmount: InvestAmount) {
            binding.investAmount = investAmount
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GridViewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val investAmount = getItem(position)
        holder.bind(investAmount)
    }


}