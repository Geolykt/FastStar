package de.geolykt.faststar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class JavaInterop {
    public static String getExecutorType() {
        return "VirtualThreadPerTask";
    }

    @NotNull
    public static Executor getExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Contract(pure = true)
    public static int getPlain(@NotNull AtomicInteger atomicInt) {
        return atomicInt.getPlain();
    }
}
