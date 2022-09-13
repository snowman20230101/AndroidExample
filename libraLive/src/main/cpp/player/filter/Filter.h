//
// Created by windy on 2022/9/9.
//

#ifndef ANDROIDEXAMPLE_FILTER_H
#define ANDROIDEXAMPLE_FILTER_H

#include "CommonInclude.h"

enum FilterType {
    NONE,
    VIDEO,
    AUDIO
};

class Filter {

public:
    Filter(AVCodecContext *codecContext, AVRational timeBase, FilterType filterType) {
        this->avCodecContext = codecContext;
        this->time_base = timeBase;
        this->filterType = filterType;
    };

    virtual ~Filter() {
        LOGD("Filter::~Filter()");
        if (filterGraph) {
            avfilter_graph_free(&filterGraph);
        }
    };

    virtual int initFilter(const char *filters_descr) = 0;

public:
    AVFilterContext *bufferSrc_ctx = NULL;
    AVFilterContext *bufferSink_ctx = NULL;
    AVCodecContext *avCodecContext = NULL;
    AVFilterGraph *filterGraph = NULL;

    AVRational time_base{};
    FilterType filterType{NONE};
};


#endif //ANDROIDEXAMPLE_FILTER_H
