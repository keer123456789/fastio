package com.keer.fastio.api;

import com.keer.fastio.api.server.HttpServer;

public class ServerApplication {
    public static void main(String[] args) {
        int port = 8080;
        HttpServer server = new HttpServer(port);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}