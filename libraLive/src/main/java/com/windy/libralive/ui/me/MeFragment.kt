package com.windy.libralive.ui.me

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseFragment
import com.windy.libralive.databinding.FragmentMeBinding

class MeFragment : BaseFragment() {
    private lateinit var viewModel: MeViewModel
    private var _binding: FragmentMeBinding? = null
    private val binding: FragmentMeBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[MeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DataBindingUtil.inflate<FragmentMeBinding>(
            inflater,
            R.layout.fragment_me,
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