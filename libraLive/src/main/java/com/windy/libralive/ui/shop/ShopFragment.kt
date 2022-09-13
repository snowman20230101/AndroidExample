package com.windy.libralive.ui.shop

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseFragment
import com.windy.libralive.data.model.home.UserBean
import com.windy.libralive.databinding.FragmentShopBinding
import com.windy.libralive.ui.FirstActivity
import com.windy.libralive.ui.MainActivity
import com.windy.libralive.ui.MediaCodecActivity
import com.windy.libralive.ui.PlayerActivity
import java.io.File

class ShopFragment : BaseFragment() {
    private lateinit var viewModel: ShopViewModel
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    private var windowManager: WindowManager? = null
    private var floatView: View? = null;

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
    ): View {
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

        initListener()

//        windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        floatView = requireActivity().layoutInflater.inflate(R.layout.window_dialog_base, null)
//
//        floatView?.findViewById<LinearLayout>(R.id.backBtn)?.setOnClickListener {
//            startActivity(Intent(requireActivity(), MainActivity::class.java))
//        }
//
//        floatView?.findViewById<LinearLayout>(R.id.overLiveBtn)?.setOnClickListener {
//            ToastUtils.showShort("退出直播")
//        }
    }

    private fun initListener() {
        // 播放器
        binding.ffmpegPlayTest.setOnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val file = File(activity?.getExternalFilesDir(""), "haoshengyin_4.mp4")
            intent.putExtra(
                "url",
//                "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv"
                file.absolutePath

            )
            startActivity(intent)
        }

        // 测试登录
        binding.loginTestBtn.setOnClickListener {
            viewModel.login("zhenchengbinbin@163.com", "zhang2281060*")
        }

        // ceshi 登录
        binding.loginTestBtn2.setOnClickListener {
            viewModel.login(UserBean("zhenchengbinbin@163.com", "zhang2281060*"))
        }

        binding.loginTestBtn3.setOnClickListener {
            startActivity(Intent(activity, FirstActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
//        if (floatView != null)
//            windowManager?.removeView(floatView)
    }

    override fun onPause() {
        super.onPause()
//        showWindowT()
    }

    @SuppressLint("InflateParams")
    fun showWindowT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireActivity())) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.setPackage("com.windy.libralive")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivityForResult(intent, 1)
//                startActivity(intent)
            }
        }

        val wmParams = WindowManager.LayoutParams()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST
        }

        wmParams.format = PixelFormat.RGBA_8888
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        wmParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        wmParams.x = 0
        wmParams.y = 0
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowManager?.addView(floatView, wmParams)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}