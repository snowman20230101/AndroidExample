//
// Created by windy on 2022/9/9.
//

#include "AudioFilter.h"

AudioFilter::AudioFilter(AVCodecContext *codecContext, AVRational timeBase)
        : Filter(codecContext, timeBase, FilterType::AUDIO) {

}

AudioFilter::~AudioFilter() {
    LOGD("AudioFilter::~AudioFilter()");
    if (filterGraph) {
        avfilter_graph_free(&filterGraph);
    }
}

int AudioFilter::initFilter(const char *filters_descr) {
    int ret = 0;

    const AVFilter *bufferSrc = avfilter_get_by_name("abuffer");
    const AVFilter *bufferSink = avfilter_get_by_name("abuffersink");
    AVFilterInOut *outputs = avfilter_inout_alloc();
    AVFilterInOut *inputs = avfilter_inout_alloc();

    // 采样位
    const enum AVSampleFormat out_sample_fmts[] = {AV_SAMPLE_FMT_S16, AV_SAMPLE_FMT_NONE};
    // 声道
    const int64_t out_channel_layouts[] = {AV_CH_LAYOUT_MONO, -1};
    // 采样率
    const int out_sample_rates[] = {8000, -1};

    filterGraph = avfilter_graph_alloc();

    if (!outputs || !inputs || !filterGraph) {
        ret = AVERROR(ENOMEM);
        goto end;
    }

    /* buffer audio source: the decoded frames from the decoder will be inserted here. */
    char args[512];

    snprintf(args, sizeof(args),
             "time_base=%d/%d:sample_rate=%d:sample_fmt=%s:channel_layout=0x%" PRIx64,
             time_base.num, time_base.den,
             avCodecContext->sample_rate,
             av_get_sample_fmt_name(avCodecContext->sample_fmt),
             avCodecContext->channel_layout
    );

    ret = avfilter_graph_create_filter(&bufferSrc_ctx, bufferSrc, "in", args, NULL, filterGraph);
    if (ret < 0) {
        LOGE("AudioFilter::initFilter() Cannot create audio buffer source.");
        goto end;
    }

    ret = avfilter_graph_create_filter(&bufferSink_ctx, bufferSink, "out", NULL, NULL, filterGraph);
    if (ret < 0) {
        LOGE("AudioFilter::initFilter() Cannot create audio buffer sink.");
        goto end;
    }

    ret = av_opt_set_int_list(bufferSink_ctx,
                              "sample_fmts",
                              out_sample_fmts,
                              -1,
                              AV_OPT_SEARCH_CHILDREN
    );
    if (ret < 0) {
        LOGE("AudioFilter::initFilter() Cannot set output sample format.");
        goto end;
    }

    ret = av_opt_set_int_list(bufferSink_ctx,
                              "channel_layouts",
                              out_channel_layouts,
                              -1,
                              AV_OPT_SEARCH_CHILDREN
    );
    if (ret < 0) {
        LOGE("AudioFilter::initFilter() Cannot set output channel layout .");
        goto end;
    }

    ret = av_opt_set_int_list(bufferSink_ctx,
                              "sample_rates",
                              out_sample_rates,
                              -1,
                              AV_OPT_SEARCH_CHILDREN
    );
    if (ret < 0) {
        LOGE("AudioFilter::initFilter() Cannot set output sample rate.");
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

    ret = avfilter_graph_parse_ptr(filterGraph, filters_descr, &inputs, &outputs, NULL);
    if (ret < 0) {
        goto end;
    }

    ret = avfilter_graph_config(filterGraph, NULL);
    if (ret < 0) {
        goto end;
    }

    end:

    /* Print summary of the sink buffer
     * Note: args buffer is reused to store channel layout string
     */
//    const AVFilterLink *outlink;
//    outlink = bufferSink_ctx->inputs[0];
//    av_get_channel_layout_string(args, sizeof(args), -1, outlink->channel_layout);
//    const char *formatName = av_get_sample_fmt_name(static_cast<AVSampleFormat>(outlink->format));
//
//    av_log(NULL, AV_LOG_INFO,
//           "Output: srate:%dHz fmt:%s chlayout:%s\n",
//           (int) outlink->sample_rate,
//           (char *) av_x_if_null(formatName, "?"),
//           args
//    );

    avfilter_inout_free(&inputs);
    avfilter_inout_free(&outputs);
    LOGD("AudioFilter::initFilter() ret=%s", av_err2str(ret));

    return ret;
}
