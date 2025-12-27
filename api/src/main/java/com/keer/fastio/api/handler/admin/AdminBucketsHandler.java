package com.keer.fastio.api.handler.admin;

import com.keer.fastio.api.entity.PathInfo;
import com.keer.fastio.api.entity.Result;
import com.keer.fastio.api.utils.RouterHandlerUtils;
import com.keer.fastio.common.entity.BucketMeta;
import com.keer.fastio.common.exception.ServiceException;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.StorageFacade;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;

/**
 * @Author: 张经伦
 * @Date: 2025/12/22  13:46
 * @Description:
 */
public class AdminBucketsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final StorageFacade facade;

    public AdminBucketsHandler(StorageFacade facade) {
        this.facade = facade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        try {
            String path = new QueryStringDecoder(fullHttpRequest.uri()).path();
            PathInfo info = new PathInfo(path);
            if (fullHttpRequest.method() == HttpMethod.PUT) {
                handlePut(channelHandlerContext, fullHttpRequest, info);
            } else if (fullHttpRequest.method() == HttpMethod.DELETE) {
                handleDelete(channelHandlerContext, fullHttpRequest, info);
            } else if (fullHttpRequest.method() == HttpMethod.GET) {
                handleGet(channelHandlerContext, fullHttpRequest, info);
            } else {
                RouterHandlerUtils.send405(channelHandlerContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            RouterHandlerUtils.send404(channelHandlerContext);
        }

    }

    /**
     * PUT 方法
     * 1. /admin/buckets/{bucket}  创建bucket
     *
     * @param channelHandlerContext
     * @param fullHttpRequest
     * @param info
     */
    private void handlePut(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, PathInfo info) {
        String bucket = info.getIndex(3);
        if (bucket == null) {
            RouterHandlerUtils.send404(channelHandlerContext);
        }
        if (bucket.equals("")) {
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.error("缺少参数")));
        }
        try {
            facade.createBucket(bucket);
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.ok()));
        } catch (ServiceException e) {
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.error(e)));
        }
    }

    /**
     * DELETE 方法
     * 1./admin/buckets/{bucket}
     *
     * @param channelHandlerContext
     * @param fullHttpRequest
     * @param info
     */
    private void handleDelete(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, PathInfo info) {
        String bucket = info.getIndex(3);
        if (bucket == null || bucket.trim().equals("")) {
            RouterHandlerUtils.send404(channelHandlerContext);
        }
        try {
            facade.deleteBucket(bucket);
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.ok()));
        } catch (ServiceException e) {
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.error(e)));
        }
    }

    /**
     * GET 方法
     * 1. /admin/buckets
     * 2. /admin/buckets/{bucket}/stat
     * 3. /admin/buckets/{bucket}/objects?prefix=a/b
     *
     * @param channelHandlerContext
     * @param fullHttpRequest
     * @param info
     */
    private void handleGet(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, PathInfo info) {
        if (info.paths.length == 3) {
            List<BucketMeta> metas = facade.listBuckets();
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.ok(metas)));
        } else if (info.paths.length == 5 && info.getIndex(4).equals("stat")) {
            BucketMeta meta = facade.headBucket(info.getIndex(3));
            RouterHandlerUtils.send200(channelHandlerContext, JsonUtil.toJson(Result.ok(meta)));
        } else if (info.paths.length == 5 && info.getIndex(4).equals("objects")) {

        } else {
            RouterHandlerUtils.send404(channelHandlerContext);
        }
    }


}
