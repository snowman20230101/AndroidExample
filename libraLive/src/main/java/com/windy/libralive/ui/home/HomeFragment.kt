package com.windy.libralive.ui.home

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.windy.libralive.R
import com.windy.libralive.base.BaseFragment
import com.windy.libralive.common.GridSpaceItemDecoration
import com.windy.libralive.data.model.Photo
import com.windy.libralive.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val images = mutableListOf<Photo>()

    private lateinit var viewModel: HomeViewModel

    private lateinit var adapter: LocalPhotoListAdapter

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[HomeViewModel::class.java]

        loadImage()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate<FragmentHomeBinding>(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LocalPhotoListAdapter(requireContext())
        binding.rv.adapter = adapter

        val dividerItemDecoration = GridSpaceItemDecoration(2, 5)
        binding.rv.addItemDecoration(dividerItemDecoration)
        viewModel.state.observe(viewLifecycleOwner) {
            adapter.mList.addAll(images)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun loadImage() {
        val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf<String>(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        val cursor: Cursor? =
            context?.contentResolver?.query(
                uriExternal,
                projection,
                null,
                null,
                MediaStore.Images.Media.DEFAULT_SORT_ORDER
            )
        if (cursor != null) {
            val id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val imageId = cursor.getString(id)
                val name = cursor.getString(displayName)
                val dataValue = cursor.getString(data)
                val withAppendedId = ContentUris.withAppendedId(uriExternal, imageId.toLong())
                if (withAppendedId != null)
                    images.add(Photo(imageId.toInt(), name, withAppendedId))

                // 464553
                // IMG_20210724_103134.jpg
                // /storage/emulated/0/DCIM/Camera/IMG_20210724_103134.jpg
                // content://media/external/images/media/464553
                Log.i("HomeFragment", "onCreate: $dataValue")
            }
        }

        viewModel.state.postValue(viewModel.state.value?.plus(1))
    }
}