package com.windy.libralive.ui.home

import android.content.ContentUris
import android.content.Context
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseFragment
import com.windy.libralive.common.GridSpaceItemDecoration
import com.windy.libralive.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

//    private val images = mutableListOf<Photo>()

    private lateinit var viewModel: HomeViewModel

    private lateinit var adapter: HomeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[HomeViewModel::class.java]

        viewModel.login("1234", "123456")
//        loadImage()
        viewModel.getArticleJson(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        adapter = HomeListAdapter()
        binding.recyclerView.adapter = adapter

        val gridSpaceItemDecoration = GridSpaceItemDecoration(2, 5)
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel.mDataBeans.observe(viewLifecycleOwner) {
            adapter.items.addAll(it.datas)
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

    private fun loadImage(context: Context) {
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
//                    images.add(Photo(imageId.toInt(), name, withAppendedId))

                // 464553
                // IMG_20210724_103134.jpg
                // /storage/emulated/0/DCIM/Camera/IMG_20210724_103134.jpg
                // content://media/external/images/media/464553
                    Log.i("HomeFragment", "onCreate: $dataValue")
            }
        }
    }
}