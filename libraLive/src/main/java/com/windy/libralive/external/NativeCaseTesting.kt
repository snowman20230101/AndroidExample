package com.windy.libralive.external

class NativeCaseTesting {

    companion object {
        /**
         * A native method that is implemented by the 'libarlive' native library,
         * which is packaged with this application.
         */
        external fun stringFromJNI(): String

        /**
         * 测试 jni throw
         */
        external fun testThrow(code: String)

        /**
         * 测试 栈益处 局部变量表
         */
        external fun testStackOverFlow(count: Int, config: String): Array<String>

        fun test() {
            val i = 5 / 0
        }


        external fun initBreakPad(path: String, logPath: String)

    }
}