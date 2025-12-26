package com.keer.fastio.api.handler.data;

import com.keer.fastio.api.utils.RouterHandlerUtils;
import com.keer.fastio.storage.StorageFacade;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

/**
 * @Author: 张经伦
 * @Date: 2025/12/23  9:00
 * @Description:
 */
public class DataHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final StorageFacade facade;

    public DataHandler(StorageFacade facade) {
        this.facade = facade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        ChannelPipeline p = ctx.pipeline();

        p.remove(this);

        String path = new QueryStringDecoder(req.uri()).path();
        if (path.startsWith("/data/object/")) {
            p.addLast(new DataObjectHandler(facade));
        } else if (path.startsWith("/data/multi/")) {
            p.addLast(new DataMultiHandler());
        }else{
            RouterHandlerUtils.send404(ctx);
            return;
        }
    }

}