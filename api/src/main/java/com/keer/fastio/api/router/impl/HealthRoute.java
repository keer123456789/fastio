package com.keer.fastio.api.router.impl;

import com.keer.fastio.api.router.BaseRoute;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张经伦
 * @date 2025/12/13 20:52
 * @description:
 */
public class HealthRoute extends BaseRoute {
    @Override
    public String getPath() {
        return "/health";
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        Map<String, String> info = new HashMap<>();
        info.put("status", "OK");
        info.put("timestamp", "" + System.currentTimeMillis());
        sendJsonResponse(ctx, info);
    }
}
