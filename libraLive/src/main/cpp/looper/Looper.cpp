//
// Created by windy on 2022/8/1.
//

//#include <__threading_support>
#include <CommonInclude.h>
#include "Looper.h"

struct LooperMessage;
typedef struct LooperMessage LooperMessage;

Looper::Looper() {
    head = nullptr;

    sem_init(&headDataAvailable, 0, 0);
    sem_init(&headWriteProtect, 0, 1);
    pthread_attr_t attr;
    pthread_attr_init(&attr);

    pthread_create(&worker, &attr, trampoline, this);
    running = true;
}

Looper::~Looper() {
    if (running) {
        LOGE("Looper deleted while still running. Some messages will not be processed");
        quit();
    }
}

void Looper::postMessage(int what, bool flush) {
    postMessage(what, 0, 0, NULL, flush);
}

void Looper::postMessage(int what, void *obj, bool flush) {
    postMessage(what, 0, 0, obj, flush);
}

void Looper::postMessage(int what, int arg1, int arg2, bool flush) {
    postMessage(what, arg1, arg2, NULL, flush);
}

void Looper::postMessage(int what, int arg1, int arg2, void *obj, bool flush) {
    LooperMessage *msg = new LooperMessage();
    msg->what = what;
    msg->obj = obj;
    msg->arg1 = arg1;
    msg->arg2 = arg2;
    msg->next = nullptr;
    msg->quit = false;
    addMessage(msg, flush);
}

void Looper::quit() {
    LOGE("Looper::quit()");
    LooperMessage *msg = new LooperMessage();
    msg->what = 0;
    msg->obj = NULL;
    msg->next = NULL;
    msg->quit = true;
    addMessage(msg, false);
    void *retval;
    pthread_join(worker, &retval);
    sem_destroy(&headDataAvailable);
    sem_destroy(&headWriteProtect);
    running = false;
}

void Looper::handleMessage(LooperMessage *msg) {
    LOGD("Looper::handleMessage [what, obj]=[%d, %p]", msg->what, msg->obj);
}

void Looper::addMessage(LooperMessage *msg, bool flush) {
    sem_wait(&headWriteProtect);
    LooperMessage *h = head;

    if (flush) {
        while (h) {
            LooperMessage *next = h->next;
            delete h;
            h = next;
        }
        h = NULL;
    }
    if (h) {
        while (h->next) {
            h = h->next;
        }
        h->next = msg;
    } else {
        head = msg;
    }
    LOGD("Looper::addMessage msg->what=%d", msg->what);
    sem_post(&headWriteProtect);
    sem_post(&headDataAvailable);
}

void *Looper::trampoline(void *p) {
    ((Looper *) p)->loop();
    return nullptr;
}

void Looper::loop(void) {
    while (true) {
        // wait for available message
        sem_wait(&headDataAvailable);

        // get next available message
        sem_wait(&headWriteProtect);
        LooperMessage *msg = head;
        if (msg == NULL) {
            LOGD("Looper::loop() no msg");
            sem_post(&headWriteProtect);
            continue;
        }
        head = msg->next;
        sem_post(&headWriteProtect);

        if (msg->quit) {
            LOGD("Looper::loop() quitting");
            delete msg;
            return;
        }
        LOGD("Looper::loop() processing msg.what=%d", msg->what);
        handleMessage(msg);
        delete msg;
    }
}
