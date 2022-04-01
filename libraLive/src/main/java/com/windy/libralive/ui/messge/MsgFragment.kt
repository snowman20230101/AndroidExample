package com.windy.libralive.ui.messge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.windy.libralive.R
import com.windy.libralive.base.BaseFragment
import com.windy.libralive.databinding.FragmentMsgBinding

class MsgFragment : BaseFragment() {

    private lateinit var viewModel: MsgViewModel
    private var _binding: FragmentMsgBinding? = null
    private val binding: FragmentMsgBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[MsgViewModel::class.java]
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}