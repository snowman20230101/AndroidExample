package com.windy.libralive.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseFragment
import com.windy.libralive.databinding.FragmentShopBinding

class ShopFragment : BaseFragment() {

    private lateinit var viewModel: ShopViewModel
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[ShopViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate<FragmentShopBinding>(
            inflater,
            R.layout.fragment_shop,
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