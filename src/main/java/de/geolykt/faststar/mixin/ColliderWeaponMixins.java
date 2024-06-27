package de.geolykt.faststar.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.geolykt.faststar.intrinsics.SpatialQuery;

import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.actors.Actor;
import snoddasmannen.galimulator.weapons.ColliderWeapon;

@Mixin(ColliderWeapon.class)
public class ColliderWeaponMixins {

    @Redirect(target = @Desc("tick"), at = @At(value = "INVOKE", desc = @Desc(owner = Space.class, value = "getNearestEnemy", args = {float.class, float.class, Actor.class, float.class}, ret = Actor.class)))
    private Actor faststar$getEnemyFast(float x, float y, @NotNull Actor witness, float radius) {
        Actor target = SpatialQuery.getNearestActor(x, y, witness);
        if (target != null) {
            x -= target.x;
            y -= target.y;
            if (x * x + y * y > radius * radius) {
                return null;
            }

            // We might want to do k-nearest-neighbour search here.
            if (target.isUntouchable() || !witness.isHostile(target)) {
                return null;
            }
        }

        return target;
    }
}
