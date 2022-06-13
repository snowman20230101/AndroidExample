package com.windy.libralive.ui.shop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseFragment
import com.windy.libralive.data.model.home.UserBean
import com.windy.libralive.databinding.FragmentShopBinding
import com.windy.libralive.ui.CameraActivity
import com.windy.libralive.ui.NetDialog
import com.windy.libralive.ui.PlayerActivity
import java.io.File

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textHome.setOnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val file = File(activity?.getExternalFilesDir(""), "haoshengyin_4.mp4")
            intent.putExtra(
                "url",
                "rtsp://admin:hik123456@10.60.193.99:554/Streaming/Channels/501?transportmode=unicast"
//                file.absolutePath

            )
            startActivity(intent)
        }

        binding.loginTestBtn.setOnClickListener {
            viewModel.login("zhenchengbinbin@163.com", "zhang2281060*")
        }

        binding.loginTestBtn2.setOnClickListener {
            viewModel.login(UserBean("zhenchengbinbin@163.com", "zhang2281060*"))
        }

        binding.loginTestBtn3.setOnClickListener {
//            NetDialog(requireActivity()).show()
//            ToastUtils.showShort("")
            startActivity(Intent(activity, CameraActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}