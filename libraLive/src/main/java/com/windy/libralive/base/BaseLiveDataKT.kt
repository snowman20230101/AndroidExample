package com.windy.libralive.base

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * 解决，LiveData 数据版本对齐问题
 * 思路：
 *  1、hook LiveData 中 mVersion 字段，因为这个版本号是每setValue（postValue）时都会递增
 *  2、hook ObserverWrapper 中的 mLastVersion 字段。（这个字段是，我们订阅的时候添加的观察者对象的父类中的字段）
 *  3、对齐这两个版本。也即是 mLastVersion = mVersion（因为）
 */
class BaseLiveDataKT<T>(val isStep: Boolean = true) : MutableLiveData<T>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
        if (isStep)
            check(observer)
    }

    private fun check(observer: Observer<in T>) {
        val liveDataClass = LiveData::class.java

        // 找到 mObservers 属性对象，并其对象值
        val mObserversField = liveDataClass.getDeclaredField("mObservers")
        mObserversField.isAccessible = true
        val mObserversValue = mObserversField.get(this)

        // 调用 mObservers 方法 get 获取其值
        val mObserversClass = mObserversValue::class.java // 字节码对象
        val getMethod = mObserversClass.getDeclaredMethod("get", Any::class.java)
        getMethod.isAccessible = true
        val observerWrapperEntry = getMethod.invoke(mObserversValue, observer)
        var observerWrapperValue: Any? = null
        if (observerWrapperEntry is Map.Entry<*, *>) {
            observerWrapperValue = observerWrapperEntry.value
        }

        if (observerWrapperValue == null) {
            throw NullPointerException("ObserverWrapper can not be null")
        }

        // mLastVersion
        val wrapperClass = observerWrapperValue::class.java.superclass
        val mLastVersionField = wrapperClass.getDeclaredField("mLastVersion")
        mLastVersionField.isAccessible = true;
        val mLastVersionValue = mLastVersionField.get(observerWrapperValue)

        val mVersionField = liveDataClass.getDeclaredField("mVersion")
        mVersionField.isAccessible = true;
        val mVersionValue = mVersionField.get(this)

        Log.d(
            "BaseLiveDataKT",
            "check: mLastVersionValue= $mLastVersionValue mVersionValue= $mVersionValue"
        )

//        mLastVersionField[observerWrapperValue] = mVersionValue
        mLastVersionField.set(observerWrapperValue, mVersionValue)
    }
}