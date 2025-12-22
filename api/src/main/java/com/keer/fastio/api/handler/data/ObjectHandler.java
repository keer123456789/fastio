package com.keer.fastio.api.handler.data;

import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.common.utils.ByteUtils;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.StorageFacade;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.request.PutObjectRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author 张经伦
 * @date 2025/12/20 10:42
 * @description: 对象任务
 */
public class ObjectHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final StorageFacade storageFacade;
    private long receivedBytes = 0;
    private ObjectWriteHandle<ObjectMeta> writeHandle;
    private WritableByteChannel writeChannel;
    private MessageDigest md5 = null;

    public ObjectHandler(StorageFacade facade) {
        this.storageFacade = facade;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            handleRequest(ctx, req);
        } else if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            handleContent(ctx, content);
        }
    }

    private void handleRequest(ChannelHandlerContext ctx, HttpRequest req) {
        if (req.method() == HttpMethod.PUT) {
            receivedBytes = 0;

        } else if (req.method() == HttpMethod.GET) {
            sendSimpleResponse(ctx, "GET OK: " + req.uri());
        } else {
            sendSimpleResponse(ctx, "Unsupported method");
        }
    }


    private void handlePut(ChannelHandlerContext ctx, HttpRequest req) {
        receivedBytes = 0;
        ObjectInfo info = parsePath(req.uri());
        if (info == null) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        PutObjectRequest request = new PutObjectRequest();
        request.setBucket(info.bucket);
        request.setKey(info.key);

        this.writeHandle = storageFacade.putObject(request);
        this.writeChannel = writeHandle.openWriteChannel();
        this.receivedBytes = 0;
    }

    private void handleContent(ChannelHandlerContext ctx, HttpContent content) {
        if (writeChannel == null) {
            return;
        }

        ByteBuf buf = content.content();
        int readable = buf.readableBytes();
        receivedBytes += readable;
        md5.update(buf.array(), buf.readerIndex(), readable);
        try {
            // ⚠️ 零拷贝写入
            buf.readBytes(Channels.newOutputStream(writeChannel), readable);
        } catch (Exception e) {
            //TODO
        }
        if (content instanceof LastHttpContent) {
            byte[] digest = md5.digest();
            String etag = ByteUtils.bytesToHex(digest);
            try {
                ObjectMeta meta = writeHandle.commit(receivedBytes, etag);
                sendOk(ctx, JsonUtil.toJson(meta));
            } catch (Exception e) {
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void sendSimpleResponse(ChannelHandlerContext ctx, String msg) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, ctx.alloc().buffer().writeBytes(msg.getBytes()));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    private ObjectInfo parsePath(String uri) {
        // /data/bucket/key/xxx
        String path = new QueryStringDecoder(uri).path();
        String[] parts = path.split("/", 4);
        if (parts.length < 4) {
            return null;
        }
        return new ObjectInfo(parts[1], parts[2]);
    }

    static class ObjectInfo {
        String bucket;
        String key;

        ObjectInfo(String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
        }
    }

    private void sendOk(ChannelHandlerContext ctx, String msg) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, ctx.alloc().buffer().writeBytes(msg.getBytes()));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }
}