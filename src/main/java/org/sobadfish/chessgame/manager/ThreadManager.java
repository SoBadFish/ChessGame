package org.sobadfish.chessgame.manager;

import java.lang.reflect.Executable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    //增加线程池
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
}
