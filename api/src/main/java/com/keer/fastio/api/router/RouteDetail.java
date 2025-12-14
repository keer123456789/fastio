package com.keer.fastio.api.router;


import com.keer.fastio.api.router.handler.HttpHandler;
import io.netty.handler.codec.http.HttpMethod;

/**
 * @author 张经伦
 * @date 2025/12/13 17:53
 * @description:
 */
public class RouteDetail {
    private String path;
    private HttpMethod method;
    private HttpHandler handler;

    public RouteDetail(String path, HttpMethod method, HttpHandler handler) {
        this.path = path;
        this.method = method;
        this.handler = handler;
    }

    public RouteDetail(BaseRoute baseRoute) {
        this.path = baseRoute.getPath();
        this.method = baseRoute.getMethod();
        this.handler = baseRoute;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpHandler getHandler() {
        return handler;
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }
}
