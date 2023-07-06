package fr.delcey.backgroundgps.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.delcey.backgroundgps.databinding.MainItemBinding

class MainAdapter : ListAdapter<MainViewState, MainAdapter.MainViewHolder>(MainDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MainViewHolder(
        MainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MainViewHolder(private val binding: MainItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MainViewState) {
            binding.mainItemTextViewLocation.text = item.location
            binding.mainItemTextViewTime.text = item.time
        }
    }

    object MainDiffUtil : DiffUtil.ItemCallback<MainViewState>() {
        override fun areItemsTheSame(oldItem: MainViewState, newItem: MainViewState): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: MainViewState, newItem: MainViewState): Boolean = oldItem == newItem
    }
}
