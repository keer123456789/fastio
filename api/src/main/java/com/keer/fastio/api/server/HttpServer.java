package com.keer.fastio.api.server;

import com.keer.fastio.api.router.Router;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author 张经伦
 * @date 2025/12/13 17:27
 * @description:
 */
public class HttpServer {
    //端口
    private int port = 8080;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        // 创建两个EventLoopGroup
        // bossGroup用于处理连接请求
        // workerGroup用于处理I/O操作
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Router router = Router.getInstance();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new HttpServerInitializer(router));

            Channel ch = b.bind(port).sync().channel();
            System.out.println("HTTP服务器启动，监听端口: " + port);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
