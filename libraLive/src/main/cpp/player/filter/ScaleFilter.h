//
// Created by windy on 2022/9/8.
//

#ifndef ANDROIDEXAMPLE_SCALEFILTER_H
#define ANDROIDEXAMPLE_SCALEFILTER_H

#include "Filter.h"

class ScaleFilter : public Filter {
public:
    ScaleFilter(AVCodecContext *codecContext, AVRational timeBase);

    ~ScaleFilter() override;

    int initFilter(const char *filters_descr) override;
};


#endif //ANDROIDEXAMPLE_SCALEFILTER_H
