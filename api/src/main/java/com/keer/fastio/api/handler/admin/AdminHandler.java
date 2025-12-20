package com.keer.fastio.api.handler.admin;

import com.keer.fastio.storage.StorageFacade;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

/**
 * @author 张经伦
 * @date 2025/12/20 10:42
 * @description:
 */
public class AdminHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final StorageFacade facade;

    public AdminHandler(StorageFacade facade) {
        this.facade = facade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        String uri = req.uri();

        if (req.method() == HttpMethod.PUT && uri.startsWith("/admin/buckets/")) {
            String bucket = uri.substring("/admin/buckets/".length());
            send(ctx, "Bucket created: " + bucket);
            return;
        }

        send(ctx, "Admin OK: " + uri);
    }

    private void send(ChannelHandlerContext ctx, String msg) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, ctx.alloc().buffer().writeBytes(msg.getBytes()));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }
}
