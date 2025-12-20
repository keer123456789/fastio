package com.keer.fastio.api.server;


import com.keer.fastio.api.ApiServer;
import com.keer.fastio.common.config.FastIoConfig;
import com.keer.fastio.storage.StorageFacade;
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
public class NettyApiServer implements ApiServer {
    //端口
    private int port = 8080;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final StorageFacade storage;

    public NettyApiServer(FastIoConfig config, StorageFacade storage) {
        if (config != null && config.getApi() != null) {
            this.port = config.getApi().getPort();
        }
        this.port = port;
        this.storage = storage;
    }

    @Override
    public void start() throws Exception {
        // 创建两个EventLoopGroup
        // bossGroup用于处理连接请求
        // workerGroup用于处理I/O操作

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyInitializer(storage));

        Channel ch = b.bind(port).sync().channel();
        System.out.println("HTTP服务器启动，监听端口: " + port);
        ch.closeFuture().sync();

    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
