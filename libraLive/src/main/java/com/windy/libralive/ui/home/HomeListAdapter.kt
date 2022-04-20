package com.windy.libralive.ui.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.windy.libralive.base.model.BaseModel
import com.windy.libralive.base.view.IBaseView
import com.windy.libralive.common.BaseBindingViewHolder
import com.windy.libralive.data.model.home.HomeBean

class HomeListAdapter : RecyclerView.Adapter<BaseBindingViewHolder>() {
    private val VIEW_TYPE_ITEM_ONE = 0
    private val VIEW_TYPE_ITEM_TWO = 1

    val items = mutableListOf<HomeBean>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseBindingViewHolder {
        return if (viewType == VIEW_TYPE_ITEM_TWO) {
            val homeItemOneView = HomeItemOneView(parent.context)
            BaseBindingViewHolder(homeItemOneView as IBaseView<BaseModel>)
        } else {
            val homeItemTwoView = HomeItemTwoView(parent.context)
            BaseBindingViewHolder(homeItemTwoView as IBaseView<BaseModel>)
        }
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        if (items[position].tags.isNotEmpty()) return VIEW_TYPE_ITEM_TWO else return VIEW_TYPE_ITEM_ONE
    }
}