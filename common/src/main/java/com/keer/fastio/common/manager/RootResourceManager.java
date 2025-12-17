package com.keer.fastio.common.manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 张经伦
 * @date 2025/12/17 21:07
 * @description:
 */
public class RootResourceManager implements Manager {
    private Map<Class<? extends AbstractResourceManager>, AbstractResourceManager> map = new ConcurrentHashMap<>();

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private static RootResourceManager instance = new RootResourceManager();
    private final List<AbstractResourceManager> initializationOrder = new ArrayList<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private RootResourceManager() {
    }

    public static RootResourceManager getInstance() {
        return instance;
    }

    public void register(final AbstractResourceManager resourceManager) {
        if (!initialized.get() && !map.containsKey(resourceManager.getClass())) {
            map.put(resourceManager.getClass(), resourceManager);
            initializationOrder.add(resourceManager);

        }
    }


    @Override
    public synchronized  void init() throws Exception {
        if (initialized.get()) {
            return;
        }
        if (closed.get()) {
            throw new IllegalStateException("Cannot initialize after close");
        }
        // 按order从小到大排序（order小的先初始化）
        Collections.sort(initializationOrder, Comparator.comparingInt(AbstractResourceManager::getOrder));

        Exception firstException = null;
        for (int i = 0; i < initializationOrder.size(); i++) {
            AbstractResourceManager manager = initializationOrder.get(i);
            try {
                manager.init();
            } catch (Exception e) {
                // 如果初始化失败，回滚已经初始化的管理器
                if (firstException == null) {
                    firstException = e;
                } else {
                    firstException.addSuppressed(e);
                }

                // 按逆序关闭已初始化的
                for (int j = i - 1; j >= 0; j--) {
                    try {
                        initializationOrder.get(j).close();
                    } catch (Exception closeEx) {
                        firstException.addSuppressed(closeEx);
                    }
                }
                break;
            }
        }

        if (firstException != null) {
            throw new Exception("Failed to initialize managers", firstException);
        }

        initialized.set(true);
    }

    @Override
    public synchronized void close() throws Exception {
        if (closed.get()) {
            return;
        }

        // 按order从大到小排序（order大的先关闭）
        List<AbstractResourceManager> reverseOrder = new ArrayList<>(initializationOrder);
        reverseOrder.sort(Comparator.comparingInt(AbstractResourceManager::getOrder).reversed());

        Exception firstException = null;
        for (AbstractResourceManager manager : reverseOrder) {
            try {
                manager.close();
            } catch (Exception e) {
                if (firstException == null) {
                    firstException = e;
                } else {
                    firstException.addSuppressed(e);
                }
            }
        }

        // 清空状态
        map.clear();
        initializationOrder.clear();
        closed.set(true);
        initialized.set(false);

        if (firstException != null) {
            throw new Exception("Failed to close all managers", firstException);
        }
    }


}
