package com.windy.libralive.ui.message

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.windy.libralive.R
import com.windy.libralive.base.BaseBindingAdapter
import com.windy.libralive.data.model.Video
import com.windy.libralive.databinding.ItemMsgListBinding

class LocalVideoListAdapter(override val context: Context) :
    BaseBindingAdapter<Video, ItemMsgListBinding>(context) {

    override fun getLayoutId(viewType: Int): Int = R.layout.item_msg_list

    override fun onBindItem(
        model: Video,
        binding: ItemMsgListBinding,
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        binding.iv.text = model.name
    }
}