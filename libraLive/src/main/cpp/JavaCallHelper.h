//
// Created by windy on 2021/12/23.
//

#ifndef LIBRALIVE_JAVACALLHELPER_H
#define LIBRALIVE_JAVACALLHELPER_H

#include "CommonInclude.h"

class JavaCallHelper {
public:
    JavaCallHelper(JavaVM *vm, JNIEnv *env, jobject instace);

    ~JavaCallHelper();

    // 回调java
    void onError(int thread, int errorCode);

    void onPrepare(int thread);

private:
    JavaVM *vm;
    JNIEnv *env;
    jobject instance;
    jmethodID onErrorId;
    jmethodID onPrepareId;
};

#endif //LIBRALIVE_JAVACALLHELPER_H
