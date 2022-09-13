//
// Created by windy on 2022/9/8.
//

#include "ScaleFilter.h"

//const char *filter_descr = "lutyuv='u=128:v=128'";
//const char *filter_descr = "boxblur";
//const char *filter_descr = "hflip";
//const char *filter_descr = "hue='h=60:s=-3'";
//const char *filter_descr = "crop=2/3*in_w:2/3*in_h";
//const char *filter_descr = "drawbox=x=100:y=100:w=100:h=100:color=pink@0.5";

ScaleFilter::ScaleFilter(AVCodecContext *codecContext, AVRational timeBase)
        : Filter(codecContext, timeBase, FilterType::VIDEO) {
    LOGD("ScaleFilter::ScaleFilter()");
}

ScaleFilter::~ScaleFilter() {
    LOGD("ScaleFilter::~ScaleFilter()");
    if (filterGraph) {
        avfilter_graph_free(&filterGraph);
    }
}

int ScaleFilter::initFilter(const char *filters_descr) {
    int ret = 0;

    const AVFilter *bufferSrc = avfilter_get_by_name("buffer");
    const AVFilter *bufferSink = avfilter_get_by_name("buffersink");
    AVFilterInOut *outputs = avfilter_inout_alloc();
    AVFilterInOut *inputs = avfilter_inout_alloc();

    enum AVPixelFormat pix_fmts[] = {AV_PIX_FMT_YUV420P, AV_PIX_FMT_NONE};

    // 为FilterGraph分配内存。
    // 多个滤镜可以组成滤镜链图（滤镜链图filtergraphs ）。
    filterGraph = avfilter_graph_alloc();

    if (!outputs || !inputs || !filterGraph) {
        LOGE("ScaleFilter::initFilter() failed.");
        ret = AVERROR(ENOMEM);
        goto end;
    }

    char args[512];
    /* buffer video source: the decoded frames from the decoder will be inserted here. */
    snprintf(args, sizeof(args),
             "video_size=%dx%d:pix_fmt=%d:time_base=%d/%d:pixel_aspect=%d/%d",
             avCodecContext->width, avCodecContext->height,
             avCodecContext->pix_fmt,
             this->time_base.num, this->time_base.den,
             avCodecContext->sample_aspect_ratio.num, avCodecContext->sample_aspect_ratio.den

    );

    // 创建并向FilterGraph中添加一个Filter。
    ret = avfilter_graph_create_filter(&bufferSrc_ctx, bufferSrc, "in", args, NULL, filterGraph);
    if (ret < 0) {
        LOGE("ScaleFilter::initFilter() Cannot create video buffer source.");
        goto end;
    }

    /* buffer video sink: to terminate the filter chain. */
    ret = avfilter_graph_create_filter(&bufferSink_ctx, bufferSink, "out", NULL, NULL, filterGraph);
    if (ret < 0) {
        LOGE("ScaleFilter::initFilter() Cannot create video buffer sink.");
        goto end;
    }

    ret = av_opt_set_int_list(bufferSink_ctx,
                              "pix_fmts",
                              pix_fmts,
                              AV_PIX_FMT_NONE,
                              AV_OPT_SEARCH_CHILDREN
    );
    if (ret < 0) {
        LOGE("ScaleFilter::initFilter() Cannot set output pixel format.");
        goto end;
    }

    /*
    * Set the endpoints for the filter graph. The filter_graph will
    * be linked to the graph described by filters_descr.
    */

    /*
     * The buffer source output must be connected to the input pad of
     * the first filter described by filters_descr; since the first
     * filter input label is not specified, it is set to "in" by
     * default.
     */
    outputs->name = av_strdup("in");
    outputs->filter_ctx = bufferSrc_ctx;
    outputs->pad_idx = 0;
    outputs->next = NULL;

    /*
     * The buffer sink input must be connected to the output pad of
     * the last filter described by filters_descr; since the last
     * filter output label is not specified, it is set to "out" by
     * default.
     */
    inputs->name = av_strdup("out");
    inputs->filter_ctx = bufferSink_ctx;
    inputs->pad_idx = 0;
    inputs->next = NULL;

    // 将一串通过字符串描述的Graph添加到FilterGraph中。
    if ((ret = avfilter_graph_parse_ptr(filterGraph, filters_descr, &inputs, &outputs, NULL)) < 0) {
        LOGE("ScaleFilter::initFilter() 添加滤镜字符串失败。");
        goto end;
    }

    // 检查FilterGraph的配置。
    if ((ret = avfilter_graph_config(filterGraph, NULL)) < 0) {
        LOGE("ScaleFilter::initFilter() 检查FilterGraph的配置失败。");
        goto end;
    }

    end:
    avfilter_inout_free(&inputs);
    avfilter_inout_free(&outputs);
    return ret;
}
