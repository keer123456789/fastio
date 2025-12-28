package com.keer.fastio.api.handler.data;

import com.keer.fastio.api.entity.PathInfo;
import com.keer.fastio.api.entity.Result;
import com.keer.fastio.api.utils.RouterHandlerUtils;
import com.keer.fastio.common.entity.MultipartUploadMeta;
import com.keer.fastio.common.exception.ServiceException;
import com.keer.fastio.common.utils.ByteUtils;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.StorageFacade;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.request.CompleteMultipartRequest;
import com.keer.fastio.storage.request.UploadPartRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author 张经伦
 * @date 2025/12/21 15:05
 * @description: 分片任务
 */
public class DataMultiHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final StorageFacade storageFacade;
    private ObjectWriteHandle<MultipartUploadMeta> writeHandle;
    private long receivedBytes = 0;
    private WritableByteChannel writeChannel;
    private MessageDigest md5 = null;


    public DataMultiHandler(StorageFacade facade) {
        this.storageFacade = facade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) httpObject;
            handleRequest(ctx, req);
        } else if (httpObject instanceof HttpContent) {
            HttpContent content = (HttpContent) httpObject;
            handleContent(ctx, content);
        }
    }

    private void handleRequest(ChannelHandlerContext ctx, HttpRequest req) {
        if (req.method() == HttpMethod.PUT) {
            handlePut(ctx, req);

        } else if (req.method() == HttpMethod.POST) {
            handlePost(ctx, req);
        } else if (req.method() == HttpMethod.DELETE) {
            handleDelete(ctx, req);
        } else {
            RouterHandlerUtils.send405(ctx);
        }
    }

    private void handlePost(ChannelHandlerContext ctx, HttpRequest req) {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri(), java.nio.charset.StandardCharsets.UTF_8);
        String path = decoder.path();
        PathInfo info = new PathInfo(path);
        Map<String, List<String>> params = decoder.parameters();
        String uploadId = getFirstParam(params, "uploadId");
        if (uploadId == null) {
            uploadId = storageFacade.initiateMultipartUpload(info.getIndex(3), info.getIndex(4));
            RouterHandlerUtils.send200(ctx, Result.ok(uploadId));
        } else {
            CompleteMultipartRequest request = new CompleteMultipartRequest();
            request.setUploadId(uploadId);
            request.setBucket(info.getIndex(3));
            try {
                storageFacade.completeMultipartUpload(request);
                RouterHandlerUtils.send200(ctx, Result.ok());
            } catch (ServiceException e) {
                RouterHandlerUtils.send200(ctx, Result.error(e));
            }
        }

    }

    private void handleDelete(ChannelHandlerContext ctx, HttpRequest req) {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri(), java.nio.charset.StandardCharsets.UTF_8);
        String path = decoder.path();
        PathInfo info = new PathInfo(path);
        Map<String, List<String>> params = decoder.parameters();
        String uploadId = getFirstParam(params, "uploadId");
        if (uploadId == null) {
            RouterHandlerUtils.send200(ctx, JsonUtil.toJson(Result.error(2000, "参数缺失")));
        } else {
            try {
                storageFacade.abortMultipartUpload(info.getIndex(3), uploadId);
                RouterHandlerUtils.send200(ctx, Result.ok());
            } catch (ServiceException e) {
                RouterHandlerUtils.send200(ctx, Result.error(e));
            }
        }

    }

    private void handlePut(ChannelHandlerContext ctx, HttpRequest req) {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri(), java.nio.charset.StandardCharsets.UTF_8);
        String path = decoder.path();
        PathInfo info = new PathInfo(path);
        Map<String, List<String>> params = decoder.parameters();
        String uploadId = getFirstParam(params, "uploadId");
        String partNumber = getFirstParam(params, "partNumber");
        if (uploadId == null || partNumber == null) {
            RouterHandlerUtils.send200(ctx, Result.error(2000, "参数缺失"));
        } else {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            UploadPartRequest request = new UploadPartRequest();
            request.setUploadId(uploadId);
            request.setBucket(info.getIndex(3));
            request.setIndex(Integer.parseInt(partNumber));
            try {

                this.writeHandle = storageFacade.uploadPart(request);
                this.writeChannel = writeHandle.openWriteChannel();
                this.receivedBytes = 0;
            } catch (ServiceException e) {
                RouterHandlerUtils.send200(ctx, Result.error(e));
            }
        }

    }

    private void handleContent(ChannelHandlerContext ctx, HttpContent content) {
        if (writeChannel == null) {
            return;
        }

        ByteBuf buf = content.content();
        int readable = buf.readableBytes();
        if (buf.hasArray()) {
            // 如果是堆内内存，直接用数组（零拷贝）
            md5.update(buf.array(), buf.arrayOffset() + buf.readerIndex(), readable);
        } else {
            // 如果是堆外内存 (Direct Buffer)，必须读出来
            byte[] bytes = new byte[readable];
            buf.getBytes(buf.readerIndex(), bytes);
            md5.update(bytes);
        }
        receivedBytes += readable;
        try {
            // ⚠️ 零拷贝写入
            buf.readBytes(Channels.newOutputStream(writeChannel), readable);
        } catch (Exception e) {
            //TODO 异常回滚？
            RouterHandlerUtils.send200(ctx, Result.error(e.getMessage()));
        }
        if (content instanceof LastHttpContent) {
            byte[] digest = md5.digest();
            String etag = ByteUtils.bytesToHex(digest);
            try {
                MultipartUploadMeta meta = writeHandle.commit(receivedBytes, etag);
                RouterHandlerUtils.send200(ctx, Result.ok(meta));
            } catch (ServiceException e) {
                RouterHandlerUtils.send200(ctx, Result.error(e));
            } catch (Exception e) {
                RouterHandlerUtils.send200(ctx, Result.error(e));
            }
        }
    }

    private String getFirstParam(Map<String, List<String>> params, String key) {
        List<String> values = params.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
