package com.keer.fastio.api.handler.data;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;

/**
 * @author 张经伦
 * @date 2025/12/21 15:05
 * @description: 分片任务
 */
public class DataMultiHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {

    }
}
