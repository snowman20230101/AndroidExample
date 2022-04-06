package com.windy.libralive.ui.message

import android.content.ContentUris
import android.database.Cursor
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
import com.windy.libralive.data.model.Video
import com.windy.libralive.databinding.FragmentMsgBinding

class MsgFragment : BaseFragment() {
    private lateinit var viewModel: MsgViewModel
    private var _binding: FragmentMsgBinding? = null
    private val binding: FragmentMsgBinding get() = _binding!!

    private lateinit var adapter: LocalVideoListAdapter

    private val videos = arrayListOf<Video>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[MsgViewModel::class.java]

        loadVideoData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate<FragmentMsgBinding>(
            inflater,
            R.layout.fragment_msg,
            container,
            false
        )
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridSpaceItemDecoration = GridSpaceItemDecoration(2, 5)
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        binding.rvMsg.addItemDecoration(dividerItemDecoration)
        adapter = LocalVideoListAdapter(requireContext())
        binding.rvMsg.adapter = adapter
        viewModel.state.observe(viewLifecycleOwner) {
            adapter.mList.addAll(videos)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadVideoData() {
        val uriExternal = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf<String>(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )

        val cursor: Cursor? =
            context?.contentResolver?.query(
                uriExternal,
                projection,
                null,
                null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER
            )
        if (cursor != null) {
            val videoId = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayName = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(videoId)
                val name = cursor.getString(displayName)
                val dataValue = cursor.getString(data)
                val withAppendedId = ContentUris.withAppendedId(uriExternal, imageId.toLong())
                Log.i("MsgFragment", "name= $name URI= $withAppendedId")

                videos.add(Video(imageId, name, withAppendedId))
            }

            viewModel.state.postValue(viewModel.state.value?.plus(1))

        }
    }
}