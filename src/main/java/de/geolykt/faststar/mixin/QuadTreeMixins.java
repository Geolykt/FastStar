package de.geolykt.faststar.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.geolykt.faststar.QuadTreePairAssembler;
import de.geolykt.faststar.QuadTreePairAssembler.QuadTree;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.QuadTreePair;

@Mixin(snoddasmannen.galimulator.QuadTree.class)
public class QuadTreeMixins implements QuadTree {
    @Overwrite
    public static Vector<QuadTreePair> generatePairsBetween(snoddasmannen.galimulator.QuadTree parentQuad, snoddasmannen.galimulator.QuadTree childQuad) {
        Vector<QuadTreePair> pairs = new Vector<>();
        QuadTreePairAssembler.assemblePairs((QuadTree) parentQuad, (QuadTree) childQuad, pairs);
        return pairs;
    }

    @Shadow
    snoddasmannen.galimulator.QuadTree northeast;
    @Shadow
    snoddasmannen.galimulator.QuadTree northwest;
    @Shadow
    snoddasmannen.galimulator.QuadTree southeast;
    @Shadow
    snoddasmannen.galimulator.QuadTree southwest;
    @Unique
    private final List<Star> storedStars = new ArrayList<>();

    @Override
    public float fastgalgen$getContainerWidth() {
        return ((snoddasmannen.galimulator.QuadTree) (Object) this).getContainerWidth();
    }

    @Override
    public Star fastgalgen$getNearestStarTo(QuadTree other) {
        return (Star) this.getNearestStar((snoddasmannen.galimulator.QuadTree) other);
    }

    @Override
    public QuadTree fastgalgen$getQuadrantA() {
        return (QuadTree) this.northwest;
    }

    @Override
    public QuadTree fastgalgen$getQuadrantB() {
        return (QuadTree) this.southwest;
    }

    @Override
    public QuadTree fastgalgen$getQuadrantC() {
        return (QuadTree) this.southeast;
    }

    @Override
    public QuadTree fastgalgen$getQuadrantD() {
        return (QuadTree) this.northeast;
    }

    @Override
    public List<Star> fastgalgen$getStarsFast() {
        return this.storedStars;
    }

    @Shadow
    private snoddasmannen.galimulator.Star getNearestStar(snoddasmannen.galimulator.QuadTree argument) {
        throw new AssertionError();
    }

    @Overwrite
    public Vector<Star> getStoredStars() {
        return new Vector<>(this.fastgalgen$getStarsFast());
    }

    @Shadow
    private float getWidth() {
        throw new AssertionError();
    }

    // TODO replace with ModifyReturnValue once we implement argument capture on it
    @Inject(method = "insert(Lsnoddasmannen/galimulator/Star;)Z", require = 1, at = @At(value = "RETURN"))
    public void onAddStoredStar(snoddasmannen.galimulator.Star capturedStar, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            this.storedStars.add((Star) capturedStar);
        }
    }
}
