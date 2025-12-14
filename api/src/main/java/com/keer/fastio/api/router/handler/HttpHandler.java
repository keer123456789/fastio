package com.keer.fastio.api.router.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpHandler {
    void handle(ChannelHandlerContext ctx, FullHttpRequest request);
}
