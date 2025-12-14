package com.keer.fastio.api.server;

import com.keer.fastio.api.router.RouteDetail;
import com.keer.fastio.api.router.Router;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @author 张经伦
 * @date 2025/12/13 17:37
 * @description:
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Router router;

    public HttpServerHandler(Router router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 获取请求信息
        String uri = request.uri();
        HttpMethod method = request.method();


        // 查找路由
        RouteDetail routeDetail = router.findRoute(uri, method);

        if (routeDetail != null) {
            routeDetail.getHandler().handle(ctx, request);
        } else {
            sendError(ctx, HttpResponseStatus.NOT_FOUND, "Route not found");
        }

    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}