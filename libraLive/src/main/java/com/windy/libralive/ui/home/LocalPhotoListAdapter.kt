package com.windy.libralive.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.windy.libralive.R
import com.windy.libralive.base.BaseBindingAdapter
import com.windy.libralive.data.model.Photo
import com.windy.libralive.databinding.ItemHomeListBinding

class LocalPhotoListAdapter(override val context: Context) :
    BaseBindingAdapter<Photo, ItemHomeListBinding>(context) {

    override fun getLayoutId(viewType: Int): Int = R.layout.item_home_list

    override fun onBindItem(
        model: Photo,
        binding: ItemHomeListBinding,
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        Glide.with(context)
            .load(model.uri)
            .placeholder(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.iv)
    }

    private fun caculateSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var sampleSize = 1;
        var picWidth = options.outWidth;
        var picHeight = options.outHeight;
        if (picWidth > reqWidth || picHeight > reqHeight) {
            var halfPicWidth = picWidth / 2;
            var halfPicHeight = picHeight / 2;
            while (halfPicWidth / sampleSize > reqWidth || halfPicHeight / sampleSize > reqHeight) {
                sampleSize *= 2;
            }
        }
        return sampleSize;
    }

}