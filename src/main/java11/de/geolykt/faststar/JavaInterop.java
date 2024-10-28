package de.geolykt.faststar;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class JavaInterop {
    public static String getExecutorType() {
        return "ForkJoinPool";
    }

    @NotNull
    public static Executor getExecutor() {
        return ForkJoinPool.commonPool()::execute;
    }

    @Contract(pure = true)
    public static int getPlain(@NotNull AtomicInteger atomicInt) {
        return atomicInt.getPlain();
    }
}
