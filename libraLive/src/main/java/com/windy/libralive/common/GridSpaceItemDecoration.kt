package com.windy.libralive.common

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpaceItemDecoration(private val spanCount: Int, private val dividerWidth: Int) :
    RecyclerView.ItemDecoration() {

    private var mDivider: Drawable? = null

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (parent.layoutManager == null || mDivider == null)
            return
    }


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val pos = parent.getChildAdapterPosition(view)

        val column = (pos) % spanCount + 1;// 计算这个child 处于第几列

        outRect.top = dividerWidth;
        outRect.bottom = 0;
        //注意这里一定要先乘 后除  先除数因为小于1然后强转int后会为0
        outRect.left = (column - 1) * dividerWidth / spanCount; // 左侧为(当前条目数-1)/总条目数*divider宽度
        outRect.right =
            (spanCount - column) * dividerWidth / spanCount;// 右侧为(总条目数-当前条目数)/总条目数*divider宽度

    }


}