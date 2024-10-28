package de.geolykt.faststar.intrinsics;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.gui.BackgroundTask;

public class TraderPopulationTask implements BackgroundTask {

    public static final float STARS_PER_TRADER = 15F;
    @NotNull
    public final BackgroundTask previousTask;
    @NotNull
    public final AtomicInteger rawProgress = new AtomicInteger();

    public TraderPopulationTask(@NotNull BackgroundTask previousTask) {
        this.previousTask = previousTask;
    }

    @Override
    @NotNull
    public String getProgressDescription() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return "Spawning Traders: " + format.format(this.getTaskProgress()) + " done.";
    }

    @Override
    public float getTaskProgress() {
        return ((float) this.rawProgress.get()) / Galimulator.getUniverse().getStarsView().size() * TraderPopulationTask.STARS_PER_TRADER;
    }
}
