package com.keer.fastio;

import com.keer.fastio.api.ApiServer;
import com.keer.fastio.api.server.NettyApiServer;
import com.keer.fastio.common.config.FastIoConfig;
import com.keer.fastio.common.manager.AbstractResourceManager;
import com.keer.fastio.common.manager.RootResourceManager;
import com.keer.fastio.storage.LocalFileStorage;
import com.keer.fastio.storage.StorageFacade;
import com.keer.fastio.storage.manager.LocalDiskManager;
import com.keer.fastio.storage.manager.ObjectLockManager;
import com.keer.fastio.storage.manager.RocksDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();

        String path = System.getProperty("config.path", "config/fastio.yaml");
        File file = new File(path);
        FastIoConfig config = null;
        if (file.exists()) {
            InputStream inputStream = new FileInputStream(path);
            config = yaml.loadAs(inputStream, FastIoConfig.class);
            inputStream.close();

        }
        if (config == null) {
            InputStream in = Main.class.getClassLoader().getResourceAsStream("fastio.yaml");
            if (in != null) {
                config = yaml.loadAs(in, FastIoConfig.class);
                in.close();
            }
        }
        RootResourceManager manager = RootResourceManager.getInstance();
        AbstractResourceManager rocksManager = new RocksDbManager(config.getStorage().getRockDb());
        manager.register(rocksManager);
        AbstractResourceManager diskManager = new LocalDiskManager(config.getStorage().getUnits());
        manager.register(diskManager);
        AbstractResourceManager objectLockManager = new ObjectLockManager();
        manager.register(objectLockManager);
        manager.init();
        StorageFacade storage = new LocalFileStorage();
        // 2️⃣ 初始化 API Server
        ApiServer apiServer = new NettyApiServer(config, storage);

        // 3️⃣ 启动
        apiServer.start();

        // （以后这里可以加）
        // - Admin Server
        // - Metrics Server
        // - Raft Node
    }
}