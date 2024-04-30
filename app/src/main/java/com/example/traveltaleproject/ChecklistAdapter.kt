//package com.example.traveltaleproject
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.traveltaleproject.databinding.ActivityChecklistItemBinding
//import kotlinx.coroutines.NonDisposableHandle.parent
//
//class ChecklistViewHolder(private val binding: ActivityChecklistItemBinding) : RecyclerView.ViewHolder(binding.root)
//
//class ChecklistAdapter(private val dataList: MutableList<String>?) : RecyclerView.Adapter<ChecklistViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
//        ChecklistViewHolder (
//            ActivityChecklistItemBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val item = dataList[position]
//        holder.bind(item)
//    }
//
//    override fun getItemCount(): Int {
//        return dataList.size
//    }
//}
//
