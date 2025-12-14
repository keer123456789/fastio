package com.keer.fastio.api.server;

import com.keer.fastio.api.router.Router;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;


/**
 * @author 张经伦
 * @date 2025/12/13 17:36
 * @description:
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private Router router;

    public HttpServerInitializer(Router router) {
        this.router = router;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP请求解码器
        pipeline.addLast("http-decoder", new HttpRequestDecoder());
        // HTTP响应编码器
        pipeline.addLast("http-encoder", new HttpResponseEncoder());
        // HTTP消息聚合器
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        // 自定义处理器
        pipeline.addLast("handler", new HttpServerHandler(this.router));
    }
}
