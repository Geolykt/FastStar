package de.geolykt.faststar.mixin;

import java.util.ArrayList;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.geolykt.faststar.FastStar;
import de.geolykt.faststar.JavaInterop;
import de.geolykt.faststar.intrinsics.EmploymentAgencyExpress;
import de.geolykt.faststar.intrinsics.SpatialQuery;
import de.geolykt.faststar.intrinsics.TraderPopulationTask;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.Star;
import de.geolykt.starloader.api.gui.BackgroundTask;

import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.QuadTree;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.actors.Actor;
import snoddasmannen.galimulator.rendersystem.RenderCache;

@Mixin(value = Space.class, priority = 3000)
public class SpaceMixins {

    @Shadow
    private static QuadTree starsQuadTree;

    @Redirect(
        target = @Desc(value = "generateGalaxy", args = {int.class, MapData.class}),
        at = @At(value = "INVOKE", desc = @Desc(owner = EmploymentAgency.class, value = "tick")),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=Generating galaxy: Setting up Dynasties step 1"),
            to = @At(value = "INVOKE", desc = @Desc(owner = snoddasmannen.galimulator.Star.class, value = "getStarlaneNeighbours", args = int.class, ret = Vector.class))
        ),
        require = 1,
        allow = 1,
        expect = 1
    )
    private static void faststar$fastInitialTick(EmploymentAgency callerInstance) {
        long timerStart = System.currentTimeMillis();

        if (callerInstance.getClass() != EmploymentAgency.class) {
            callerInstance.tick();
        } else {
            EmploymentAgencyExpress.populateGalaxy(callerInstance);
        }

        long time = System.currentTimeMillis() - timerStart;
        LoggerFactory.getLogger(FastStar.class).info("Time taken for initial tick (dynasties step 1): {} ms", time);
    }

    @Inject(target = @Desc(value = "getNearestEnemy", args = {float.class, float.class, Actor.class, float.class}, ret = Actor.class), at = @At("HEAD"), cancellable = true)
    private static void faststar$getNearestEnemyFast(float x, float y, @NotNull Actor origin, float radius, CallbackInfoReturnable<Actor> cir) {
        Actor a = SpatialQuery.getNearestActorHostile(x, y, origin);
        cir.setReturnValue((a == null || a.getDistanceToPoint(x, y) > radius) ? null : a);
    }

    @Inject(
        target = @Desc(value = "drawToCache", ret = RenderCache.class),
        at = @At(value = "FIELD", desc = @Desc(value = "actors", ret = Vector.class)),
        allow = 1,
        require = 1
    )
    private static void faststar$onActorDraw(CallbackInfoReturnable<RenderCache> cir) {
        SpatialQuery.updateStarsActorDrawing();
    }

    @Inject(
        target = @Desc(owner = Space.class, value = "generateGalaxy", args = {int.class, MapData.class}, ret = void.class),
        at = @At(value = "NEW", target = "Lsnoddasmannen/galimulator/actors/Trader;<init> (Lsnoddasmannen/galimulator/Star;)Lsnoddasmannen/galimulator/actors/Trader;"),
        allow = 1,
        require = 1
    )
    private static void faststar$onTraderCreation(CallbackInfo ci) {
        BackgroundTask task = Galimulator.getBackgroundTask();
        TraderPopulationTask tpt;
        if (task instanceof TraderPopulationTask) {
            tpt = (TraderPopulationTask) task;
        } else {
            Galimulator.setBackgroundTask(tpt = new TraderPopulationTask(task));
        }
        tpt.rawProgress.set(JavaInterop.getPlain(tpt.rawProgress) + 1);
    }

    @Inject(
        target = @Desc(owner = Space.class, value = "generateGalaxy", args = {int.class, MapData.class}, ret = void.class),
        at = @At(value = "CONSTANT", args = "intValue=0"),
        slice = {
            @Slice(from = @At(value = "NEW", target = "Lsnoddasmannen/galimulator/actors/Trader;<init> (Lsnoddasmannen/galimulator/Star;)Lsnoddasmannen/galimulator/actors/Trader;"), to = @At(value = "INVOKE", desc = @Desc(value = "getRandomStar", ret = snoddasmannen.galimulator.Star.class)))
        },
        allow = 1,
        require = 1
    )
    private static void faststar$postTraderCreation(CallbackInfo ci) {
        Galimulator.setBackgroundTask(((TraderPopulationTask) Galimulator.getBackgroundTask()).previousTask);
    }

    @Inject(
        method = "Lsnoddasmannen/galimulator/Space; tick()I",
        at = @At(value = "FIELD", desc = @Desc(value = "actors", ret = Vector.class)),
        slice = @Slice(
            from = @At(value = "INVOKE", desc = @Desc(owner = snoddasmannen.galimulator.Alliance.class, value = "kill")),
            to = @At(value = "INVOKE", desc = @Desc(owner = Actor.class, value = "activity"))
        ),
        allow = 1,
        require = 1
    )
    private static void faststar$updateActorLoop(CallbackInfoReturnable<Integer> cir) {
        SpatialQuery.updateActorsActorTicking();
    }

    @Overwrite
    public static ArrayList<Star> getStarsNear(float x, float y, int count) {
        ArrayList<Star> stars = new ArrayList<>();
        SpatialQuery.getNearestStars(x, y, count, stars);
        return stars;
    }
}
