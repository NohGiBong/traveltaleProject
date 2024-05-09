package com.example.traveltaleproject.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.checklist.CheckListAdapter

class SwipeToDeleteCallbackCheckList(private val adapter: CheckListAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (adapter.canSwipeItem(position)) {
            adapter.removeEmptyItem(position)
        } else {
            adapter.notifyItemChanged(position) // 스와이프를 취소하고 아이템을 다시 보이게 합니다.
        }
    }
}