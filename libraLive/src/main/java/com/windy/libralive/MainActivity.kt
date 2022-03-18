package com.windy.libralive

import com.windy.libralive.databinding.ActivityMainBinding;

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnVedio.setOnClickListener(this)
        binding.btnRtmp.setOnClickListener(this)
        binding.btnTest.setOnClickListener(this)

        // Example of a call to a native method
        binding.ffmpegVersion.text = stringFromJNI()
    }

    /**
     * A native method that is implemented by the 'libarlive' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    private external fun testThrow(code: String)

    fun test() {
        val i = 5 / 0
    }

    companion object {
        // Used to load the 'libarlive' library on application startup.
        init {
            System.loadLibrary("libralive")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_rtmp -> {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("url", "http://cctvalih5ca.v.myalicdn.com/live/cctv1_2/index.m3u8")
                startActivity(intent)
            }

            R.id.btn_vedio -> {
                val intent = Intent(this, PlayerActivity::class.java)
                val file = File(getExternalFilesDir(""), "haoshengyin_4.mp4")
                intent.putExtra("url", file.absolutePath);
                startActivity(intent)
            }

            R.id.btn_test -> {
                testThrow("异常测试")
            }
        }
    }
}