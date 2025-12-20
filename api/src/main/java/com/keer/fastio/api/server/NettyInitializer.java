package com.keer.fastio.api.server;

import com.keer.fastio.api.handler.RouterHandler;
import com.keer.fastio.storage.StorageFacade;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author 张经伦
 * @date 2025/12/20 15:06
 * @description:
 */
public class NettyInitializer extends ChannelInitializer<SocketChannel> {

    private final StorageFacade storage;

    public NettyInitializer(StorageFacade storage) {
        this.storage = storage;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new RouterHandler(storage));
    }
}
