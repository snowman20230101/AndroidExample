//
// Created by windy on 2022/9/7.
//

#include "Utils.h"
#include "CommonInclude.h"


bool DumpCallback(const google_breakpad::MinidumpDescriptor &descriptor,
                  void *context, bool succeeded) {
    LOGE("ndk_crash Dump path: %s", descriptor.path());

    // 如果回调返回true，Breakpad将把异常视为已完全处理，禁止任何其他处理程序收到异常通知。
    // 如果回调返回false，Breakpad会将异常视为未处理，并允许其他处理程序处理它。 return false;
    return false;
}

void Utils::initBreakPad(const char *path) {
    LOGD("Utils::initBreakPad() path=%s", path);
    google_breakpad::MinidumpDescriptor descriptor(path);
    static google_breakpad::ExceptionHandler eh(descriptor,
                                                nullptr,
                                                DumpCallback,
                                                nullptr,
                                                true,
                                                -1
    );
}
