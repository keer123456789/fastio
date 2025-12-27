package com.keer.fastio.api.handler;


import com.keer.fastio.api.handler.admin.AdminBucketsHandler;
import com.keer.fastio.api.handler.data.DataMultiHandler;
import com.keer.fastio.api.handler.data.DataObjectHandler;
import com.keer.fastio.api.utils.RouterHandlerUtils;
import com.keer.fastio.storage.StorageFacade;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;


/**
 * @author 张经伦
 * @date 2025/12/13 17:37
 * @description:
 */
public class RouterHandler extends ChannelInboundHandlerAdapter {

    private final StorageFacade facade;

    public RouterHandler(StorageFacade facade) {
        this.facade = facade;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof HttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }
        HttpRequest req=(HttpRequest)msg;
        String path = new QueryStringDecoder(req.uri()).path();
        ChannelPipeline p = ctx.pipeline();

        if (path.startsWith("/data/object")) {
            p.addLast(new DataObjectHandler(facade));
        } else if (path.startsWith("/data/multi")) {
            p.addLast(new DataMultiHandler(facade));
        } else if (path.startsWith("/admin/buckets")) {
            // ⚠️ 插在 HttpServerCodec 后面
            p.addLast(new HttpObjectAggregator(1024 * 1024));
            p.addLast(new AdminBucketsHandler(facade));
        }
        // 移除路由自己，后续的 Content 消息将不再经过 Router
        // 移除后，Pipeline 结构变为：Codec -> [New Handlers]
        p.remove(this);

        // ⚠️ 修正点2：只触发一次，将当前的 HttpRequest 传给刚才添加的 Handler
        ctx.fireChannelRead(msg);
    }


}