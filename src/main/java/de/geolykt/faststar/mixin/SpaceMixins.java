package de.geolykt.faststar.mixin;

import java.util.ArrayList;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.geolykt.faststar.intrinsics.SpatialQuery;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.QuadTree;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.actors.Actor;

@Mixin(value = Space.class, priority = 3000)
public class SpaceMixins {

    @Shadow
    private static QuadTree starsQuadTree;

    @Overwrite
    public static ArrayList<Star> getStarsNear(float x, float y, int count) {
        ArrayList<Star> stars = new ArrayList<>();
        SpatialQuery.getNearestStars(x, y, count, stars);
        return stars;
    }

    @Inject(target = @Desc(value = "getNearestEnemy", args = {float.class, float.class, Actor.class, float.class}, ret = Actor.class), at = @At("HEAD"), cancellable = true)
    private static void faststar$getNearestEnemyFast(float x, float y, @NotNull Actor origin, float radius, CallbackInfoReturnable<Actor> cir) {
        Actor a = SpatialQuery.getNearestActorHostile(x, y, origin);
        cir.setReturnValue((a == null || a.getDistanceToPoint(x, y) > radius) ? null : a);
    }

    @Inject(
        method = "Lsnoddasmannen/galimulator/Space; tick()I",
        at = @At(value = "FIELD", desc = @Desc(value = "actors", ret = Vector.class)),
        slice = @Slice(
            from = @At(value = "INVOKE", desc = @Desc(owner = snoddasmannen.galimulator.Alliance.class, value = "kill")),
            to = @At(value = "INVOKE", desc = @Desc(owner = Actor.class, value = "activity"))
        )
    )
    private static void faststar$updateActorLoop(CallbackInfo ci) {
        SpatialQuery.updateActorsActorTicking();
    }
}
