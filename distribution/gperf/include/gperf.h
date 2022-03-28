#ifndef __GPERF_HPP__
#define __GPERF_HPP__

#include "sys/types.h"

#ifdef __cplusplus
extern "C" {
#endif

int add(int x, int y);

/*
* return current system ticks
*/
uint64_t GetTicks(void);

#ifdef __cplusplus
};
#endif

#endif /* __GPERF_HPP__ */