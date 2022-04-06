package com.windy.libralive.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * 适配器基类
 */
abstract class BaseBindingAdapter<Model, ViewBinding : ViewDataBinding>(
    protected open val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    val mList = ArrayList<Model>()

    /**
     * 填充数据
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /**
         * TODO 调用这里出现的问题 appears 浮现出现 inaccessible 不可接受的
         * java.lang.IllegalAccessError: Field 'com.windy.libralive.common.BaseBindingAdapter$BaseHolderView.itemView'
         *      is inaccessible to class 'com.windy.libralive.common.BaseBindingAdapter'
         *      (declaration of 'com.windy.libralive.common.BaseBindingAdapter' appears in /data/app/com.windy.libralive-98idBpWpHcetb1jAvPCBqA==/base.apk!classes5.dex)
         */
        val binding: ViewBinding? = DataBindingUtil.getBinding<ViewBinding>(holder.itemView)
        val model = mList[position]
        onBindItem(model, binding!!, holder, position)
        binding!!.executePendingBindings() // 计算挂起的绑定，更新任何将表达式绑定到修改过的变量的视图。这必须在UI线程上运行。
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewBinding = DataBindingUtil.inflate<ViewBinding>(
            LayoutInflater.from(context),
            getLayoutId(viewType),
            parent,
            false
        )

        return BaseBindingViewHolder2(binding.root)
    }

    override fun getItemCount(): Int = mList.size

    protected abstract fun getLayoutId(viewType: Int): Int

    protected abstract fun onBindItem(
        model: Model,
        binding: ViewBinding,
        holder: RecyclerView.ViewHolder,
        position: Int
    )

    class BaseBindingViewHolder2(private val itemView: View) : RecyclerView.ViewHolder(itemView)
}

