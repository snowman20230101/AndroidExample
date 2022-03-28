#include "gperf.h"
#include <time.h>

int add(int x, int y) {
    return x + y;
}

uint64_t GetTicks(void) {
    struct timeval Time;

    uint64_t cur_tick = (uint64_t) 1000000;

    gettimeofday(&Time, NULL);

    cur_tick *= Time.tv_sec;

    return (cur_tick + Time.tv_usec);
}