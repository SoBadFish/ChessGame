package org.sobadfish.chessgame.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    // 保留线程池工具，避免旧调用点在热重载后持有失效线程池实例。
    public static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static synchronized void reset() {
        shutdown();
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
