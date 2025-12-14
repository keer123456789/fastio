package com.keer.fastio.api.router;


import com.keer.fastio.api.router.handler.HttpHandler;
import com.keer.fastio.api.router.impl.HealthRoute;
import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 张经伦
 * @date 2025/12/13 19:43
 * @description:
 */
public class Router {
    private final List<RouteDetail> routeDetails = new ArrayList<>();

    private final static Router _router = new Router();

    private Router() {
        init();
    }

    public static Router getInstance() {
        return _router;
    }

    /**
     * 路由管理器初始化
     * TODO 手动写入路由
     */
    private void init() {
        this.addRoute(new HealthRoute());
    }

    public void addRoute(BaseRoute route) {
        routeDetails.add(new RouteDetail(route));
    }

    public void addRoute(RouteDetail routeDetail) {
        routeDetails.add(routeDetail);
    }

    public void addRoute(String path, HttpMethod method, HttpHandler handler) {
        addRoute(new RouteDetail(path, method, handler));
    }

    public void get(String path, HttpHandler handler) {
        addRoute(path, HttpMethod.GET, handler);
    }

    public void post(String path, HttpHandler handler) {
        addRoute(path, HttpMethod.POST, handler);
    }

    public RouteDetail findRoute(String path, HttpMethod method) {
        return routeDetails.stream().filter(routeDetail -> routeDetail.getPath().equals(path) && routeDetail.getMethod().equals(method)).findFirst().orElse(null);
    }
}
