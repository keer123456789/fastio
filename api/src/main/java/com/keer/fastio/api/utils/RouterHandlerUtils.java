package com.keer.fastio.api.utils;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

/**
 * @Author: 张经伦
 * @Date: 2025/12/22  13:54
 * @Description:
 */
public class RouterHandlerUtils {
    public static void send404(ChannelHandlerContext ctx) {
        FullHttpResponse resp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND
        );
        send(ctx, resp);
    }

    public static void send405(ChannelHandlerContext ctx) {
        FullHttpResponse resp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.METHOD_NOT_ALLOWED
        );
        send(ctx, resp);
    }

    public static void send200(ChannelHandlerContext ctx, String jsonData) {
        FullHttpResponse resp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.METHOD_NOT_ALLOWED,
                ctx.alloc().buffer().writeBytes(jsonData.getBytes())
        );
        send(ctx, resp);
    }

    public static void send(ChannelHandlerContext ctx, FullHttpResponse resp) {
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }
}
