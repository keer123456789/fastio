package com.keer.fastio.api.utils;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

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
        send(ctx, resp,0);
    }

    public static void send405(ChannelHandlerContext ctx) {
        FullHttpResponse resp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.METHOD_NOT_ALLOWED
        );
        send(ctx, resp,0);
    }

    public static void send200(ChannelHandlerContext ctx, String jsonData) {
        byte[] jsonBytes = jsonData.getBytes(StandardCharsets.UTF_8);
        FullHttpResponse resp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                ctx.alloc().buffer().writeBytes(jsonBytes)
        );
        send(ctx, resp,jsonBytes.length);
    }

    public static void send(ChannelHandlerContext ctx, FullHttpResponse resp,int contentLength) {
        if (contentLength > 0) {
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
        }
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }
}
