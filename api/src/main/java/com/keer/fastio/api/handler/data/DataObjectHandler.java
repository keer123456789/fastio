package com.keer.fastio.api.handler.data;

import com.keer.fastio.api.entity.PathInfo;
import com.keer.fastio.api.entity.Result;
import com.keer.fastio.api.utils.RouterHandlerUtils;
import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.common.exception.ServiceException;
import com.keer.fastio.common.utils.ByteUtils;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.StorageFacade;
import com.keer.fastio.storage.handle.ObjectReadHandle;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.request.GetObjectRequest;
import com.keer.fastio.storage.request.PutObjectRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author 张经伦
 * @date 2025/12/20 10:42
 * @description: 对象任务
 */
public class DataObjectHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final StorageFacade storageFacade;
    private long receivedBytes = 0;
    private ObjectWriteHandle<ObjectMeta> writeHandle;
    private WritableByteChannel writeChannel;
    private MessageDigest md5 = null;

    public DataObjectHandler(StorageFacade facade) {
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
            handlePut(ctx, req);
        } else if (req.method() == HttpMethod.GET) {
            handleGet(ctx, req);
        } else if (req.method() == HttpMethod.HEAD) {
            handleHead(ctx, req);
        } else if (req.method() == HttpMethod.DELETE) {
            handleDelete(ctx, req);
        } else {
            RouterHandlerUtils.send405(ctx);
        }
    }

    /**
     * /data/object/{bucket}/{objectKey}
     * PUT 获取元数据
     *
     * @param ctx
     * @param req
     */
    private void handlePut(ChannelHandlerContext ctx, HttpRequest req) {
        receivedBytes = 0;
        PathInfo info = new PathInfo(req.uri());

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        PutObjectRequest request = new PutObjectRequest();
        request.setBucket(info.getIndex(3));
        request.setKey(info.getIndex(4));

        this.writeHandle = storageFacade.putObject(request);
        this.writeChannel = writeHandle.openWriteChannel();
        this.receivedBytes = 0;
    }

    /**
     * /data/object/{bucket}/{objectKey}
     * PUT 流
     *
     * @param ctx
     * @param content
     */
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
            RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.error(e)));
        }
        if (content instanceof LastHttpContent) {
            byte[] digest = md5.digest();
            String etag = ByteUtils.bytesToHex(digest);
            try {
                ObjectMeta meta = writeHandle.commit(receivedBytes, etag);
                RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.ok(meta)));
            } catch (ServiceException e) {
                RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.error(e)));
            } catch (Exception e) {
                RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.error(e)));
            }
        }
    }

    /**
     * /data/object/{bucket}/{objectKey}
     * PUT 获取元数据
     *
     * @param ctx
     * @param req
     */
    private void handleGet(ChannelHandlerContext ctx, HttpRequest req) {
        String path = new QueryStringDecoder(req.uri()).path();
        PathInfo info = new PathInfo(path);
        GetObjectRequest request = new GetObjectRequest();
        request.setBucket(info.getIndex(3));
        request.setKey(info.getIndex(4));
        ObjectReadHandle handle = storageFacade.getObject(request);
        FileChannel fileChannel = (FileChannel) handle.openChannel();
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, handle.mimeType());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, handle.contentLength());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.write(response);
        long position = 0;
        long count = handle.contentLength();
        DefaultFileRegion region = new DefaultFileRegion(fileChannel, position, count);

        // 3. 写出文件区域
        ChannelFuture sendFileFuture = ctx.write(region);

        // 4. 添加监听器，传输完成后关闭文件通道
        sendFileFuture.addListener((ChannelFutureListener) future -> {
            // 如果不是长连接，则关闭 Channel
            if (!HttpUtil.isKeepAlive(req)) {
                handle.close();
            }
        });
        // 5. 写入结束标记
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

    }

    private void handleHead(ChannelHandlerContext ctx, HttpRequest req) {
        String path = new QueryStringDecoder(req.uri()).path();
        PathInfo info = new PathInfo(path);
        ObjectMeta meta = storageFacade.headObject(info.getIndex(3), info.getIndex(4));
        RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.ok(meta)));
    }

    private void handleDelete(ChannelHandlerContext ctx, HttpRequest req) {
        String path = new QueryStringDecoder(req.uri()).path();
        PathInfo info = new PathInfo(path);
        try {
            storageFacade.deleteObject(info.getIndex(3), info.getIndex(4));
            RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.ok()));
        } catch (ServiceException e) {
            RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.error(e)));
        }

    }

}