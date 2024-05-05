package com.example.traveltaleproject

import android.content.Context
import android.view.View.getDefaultSize
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScheduleItemLayoutManager(context: Context) : LinearLayoutManager(context, VERTICAL, false) {
    private var totalHeight = 0

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)

        totalHeight = 0
        for (i in 0 until itemCount) {
            val item = recycler.getViewForPosition(i)
            measureChildWithMargins(item, 0, 0)
            totalHeight += getDecoratedMeasuredHeight(item)
            recycler.recycleView(item)
        }

        setMeasuredDimension(getDefaultSize(widthSpec, widthSpec), totalHeight)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        for (i in 0 until itemCount) {
            val item = recycler.getViewForPosition(i)
            addView(item)
            measureChildWithMargins(item, 0, 0)
            layoutDecorated(item, 0, 0, item.measuredWidth, item.measuredHeight)
        }
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
