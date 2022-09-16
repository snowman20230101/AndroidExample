//
// Created by windy on 2021/12/22.
//

#ifndef LIBRALIVE_MESSAGE_QUEUE_H
#define LIBRALIVE_MESSAGE_QUEUE_H

#include <pthread.h>
#include <queue>
#include <android/log.h>

/**
 * 消息处理封装
 * @tparam T
 */

template<typename T>
class message_queue {
    typedef void (*ReleaseCallBack)(T &);

    typedef void (*SyncHandle)(std::queue<T> &);

public:
    message_queue() {
        pthread_mutex_init(&mutex, NULL);
        pthread_cond_init(&cond, NULL);
    }

    ~message_queue() {
        pthread_cond_destroy(&cond);
        pthread_mutex_destroy(&mutex);
    }

    /**
     * push 数据
     *
     * @param value
     */
    void push(T value) {
        pthread_mutex_lock(&mutex);
        if (state) {
            queue.push(value);
            pthread_cond_signal(&cond);
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, "FFMPEG", "push() 无法加入数据 queue size is %d",
                                queue.size()
            );
//            if (releaseCallBack)
                releaseCallBack(value);
        }

        pthread_mutex_unlock(&mutex);
    }

    /**
     * 取出消息
     *
     * @param value
     * @return
     */
    int pop(T &value) {
        int ret = 0;
        pthread_mutex_lock(&mutex);
        // 在多核处理器下 由于竞争可能虚假唤醒 包括jdk也说明了
        while (state && queue.empty()) {
            pthread_cond_wait(&cond, &mutex); // 释放锁的等待。
        }

        if (!queue.empty()) {
            value = queue.front();
            queue.pop();
            ret = 1;
        }
        pthread_mutex_unlock(&mutex);
        return ret;
    }

    unsigned int size() {
        return queue.size();
    }

    int empty() {
        return queue.empty();
    }

    void clear() {
        __android_log_print(ANDROID_LOG_DEBUG, "FFMPEG", "message_queue.h clear()");
        pthread_mutex_lock(&mutex);

        while (!queue.empty()) {
            T value = queue.front();
//            if (releaseCallBack)
                releaseCallBack(value);
            queue.pop();
        }
        pthread_mutex_unlock(&mutex);
    }

    void gotoWork() {
        pthread_mutex_lock(&mutex);
        this->state = true;
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&mutex);
    }

    void backWork() {
        pthread_mutex_lock(&mutex);
        this->state = false;
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&mutex);
    }

    void setReleaseCallBack(ReleaseCallBack callBack) {
        this->releaseCallBack = callBack;
    }

    void setSyncHandle(SyncHandle handle) {
        this->syncHandle = handle;
    }

    void sync() {
        pthread_mutex_lock(&mutex);
//        if (syncHandle)
            syncHandle(queue);
        pthread_mutex_unlock(&mutex);
    }

private:
    pthread_mutex_t mutex;
    pthread_cond_t cond;

    // 函数指针定义 释放回调
    ReleaseCallBack releaseCallBack;

    // 函数指针定义 同步容器回调
    SyncHandle syncHandle;

    std::queue<T> queue;

    // 当前状态
    bool state{false};
};

#endif //LIBRALIVE_MESSAGE_QUEUE_H
