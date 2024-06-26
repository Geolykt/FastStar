package de.geolykt.faststar;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import org.jetbrains.annotations.NotNull;

public class JavaInterop {
    public static String getExecutorType() {
        return "ForkJoinPool";
    }

    @NotNull
    public static Executor getExecutor() {
        return ForkJoinPool.commonPool()::execute;
    }
}
