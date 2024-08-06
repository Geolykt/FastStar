package de.geolykt.faststar.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import de.geolykt.faststar.intrinsics.LandmarkPopulator.Landmark;
import de.geolykt.starloader.api.empire.Star;

@Mixin(value = snoddasmannen.galimulator.guides.Landmark.class, priority = 3000)
public class LandmarkMixins implements Landmark {
    @Shadow
    private snoddasmannen.galimulator.Star landmarkStar;

    @Shadow
    private Map<Star, Float> starlaneDistances;

    @Override
    @Unique(silent = true) // Behaves like @Intrinsic here
    public Star getCenter() {
        return (Star) this.landmarkStar;
    }

    @Override
    @Unique(silent = true) // Behaves like @Intrinsic here
    public Map<Star, Float> getDistances() {
        return this.starlaneDistances;
    }
}
