package com.keer.fastio.api.handler;


import com.keer.fastio.api.handler.admin.AdminHandler;
import com.keer.fastio.api.handler.data.DataObjectHandler;
import com.keer.fastio.api.utils.RouterHandlerUtils;
import com.keer.fastio.storage.StorageFacade;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;


/**
 * @author 张经伦
 * @date 2025/12/13 17:37
 * @description:
 */
public class RouterHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final StorageFacade facade;

    public RouterHandler(StorageFacade facade) {
        this.facade = facade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest req) {
        String path = new QueryStringDecoder(req.uri()).path();
        ChannelPipeline p = ctx.pipeline();

        p.remove(this);
        if (path.startsWith("/data/")) {
            p.addLast(new DataObjectHandler(facade));
        } else if (path.startsWith("/admin/")) {
            p.addLast(new HttpObjectAggregator(1024 * 1024));
            p.addLast(new AdminHandler(facade));
        } else {
            RouterHandlerUtils.send404(ctx);
            return;
        }

        ctx.fireChannelRead(req);
    }

}