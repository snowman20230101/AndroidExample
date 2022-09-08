//
// Created by windy on 2022/9/8.
//

#ifndef ANDROIDEXAMPLE_SCALEFILTER_H
#define ANDROIDEXAMPLE_SCALEFILTER_H

#include "CommonInclude.h"

class ScaleFilter {
public:
    ScaleFilter(AVCodecContext *codecContext, AVRational timeBase);

    ~ScaleFilter();

    int initFilter(const char *filters_descr);

public:
    AVFilterContext *bufferSrc_ctx = NULL;
    AVFilterContext *bufferSink_ctx = NULL;
    AVCodecContext *avCodecContext = NULL;
    AVFilterGraph *filterGraph = NULL;

    AVRational time_base{};
};


#endif //ANDROIDEXAMPLE_SCALEFILTER_H
