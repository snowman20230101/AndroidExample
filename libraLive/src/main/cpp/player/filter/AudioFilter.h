//
// Created by windy on 2022/9/9.
//

#ifndef ANDROIDEXAMPLE_AUDIOFILTER_H
#define ANDROIDEXAMPLE_AUDIOFILTER_H

#include "Filter.h"

class AudioFilter : public Filter {
public:
    AudioFilter(AVCodecContext *codecContext, AVRational timeBase);
    ~AudioFilter() override;

    int initFilter(const char *filters_descr) override;
};


#endif //ANDROIDEXAMPLE_AUDIOFILTER_H
