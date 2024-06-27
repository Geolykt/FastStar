package de.geolykt.faststar.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.badlogic.gdx.math.Rectangle;

import de.geolykt.faststar.QuadTreePairAssembler;
import de.geolykt.faststar.QuadTreePairAssembler.QuadTree;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.QuadTreePair;

@Mixin(value = snoddasmannen.galimulator.QuadTree.class, priority = 3000)
public class QuadTreeMixins implements QuadTree {
    @Overwrite
    public static Vector<QuadTreePair> generatePairsBetween(snoddasmannen.galimulator.QuadTree parentQuad, snoddasmannen.galimulator.QuadTree childQuad) {
        Vector<QuadTreePair> pairs = new Vector<>();
        QuadTreePairAssembler.assemblePairs((QuadTree) parentQuad, (QuadTree) childQuad, pairs);
        return pairs;
    }

    @Shadow
    private snoddasmannen.galimulator.QuadTree northeast;
    @Shadow
    private snoddasmannen.galimulator.QuadTree northwest;
    @Shadow
    private snoddasmannen.galimulator.QuadTree southeast;
    @Shadow
    private snoddasmannen.galimulator.QuadTree southwest;
    @Shadow
    private snoddasmannen.galimulator.Star storedStar;
    @Unique
    private final List<Star> storedStars = new ArrayList<>();
    @Shadow
    private float x1;
    @Shadow
    private float x2;
    @Shadow
    private float y1;
    @Shadow
    private float y2;

    @Override
    @Unique
    public final float faststar$getContainerWidth() {
        return ((snoddasmannen.galimulator.QuadTree) (Object) this).getContainerWidth();
    }

    @Override
    @Unique
    public final Star faststar$getNearestStarTo(QuadTree other) {
        return (Star) this.getNearestStar((snoddasmannen.galimulator.QuadTree) other);
    }

    @Override
    @Unique
    public final QuadTree faststar$getQuadrantA() {
        return (QuadTree) this.northwest;
    }

    @Override
    @Unique
    public final QuadTree faststar$getQuadrantB() {
        return (QuadTree) this.southwest;
    }

    @Override
    @Unique
    public final QuadTree faststar$getQuadrantC() {
        return (QuadTree) this.southeast;
    }

    @Override
    @Unique
    public final QuadTree faststar$getQuadrantD() {
        return (QuadTree) this.northeast;
    }

    @Override
    @Unique
    public final List<Star> faststar$getStarsFast() {
        return this.storedStars;
    }

    @Inject(target = @Desc(value = "getStarsWithin", args = {Vector.class, Rectangle.class}), at = @At("HEAD"), cancellable = true)
    private final void faststar$getStarsWithinFullOverlap(Vector<Star> output, Rectangle bounds, CallbackInfo ci) {
        if (bounds.contains(this.x1, this.y1) && bounds.contains(this.x2, this.y2)) {
            output.addAll(this.faststar$getStarsFast());
            ci.cancel();
        }
    }

    // TODO replace with ModifyReturnValue once we implement argument capture on it
    @Inject(method = "insert(Lsnoddasmannen/galimulator/Star;)Z", require = 1, at = @At(value = "RETURN"))
    private final void faststar$onAddStoredStar(snoddasmannen.galimulator.Star capturedStar, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            this.storedStars.add((Star) capturedStar);
        }
    }

    /*
    @Redirect(
        target = @Desc(value = "getStarsWithin", args = {Vector.class, Rectangle.class}),
        at = @At(value = "INVOKE", desc = @Desc(value = "isOverlapping", args = {Rectangle.class}, ret = boolean.class)),
        slice = @Slice(from = @At(value = "FIELD", desc = @Desc(value = "storedStar", ret = snoddasmannen.galimulator.Star.class)), to = @At("TAIL")),
        allow = 1,
        expect = 1
    )
    private final boolean faststar$shortcircuitDuplicateOverlapCheck(snoddasmannen.galimulator.QuadTree qtree, Rectangle r) {
        return true;
    }*/

    @Shadow
    private final snoddasmannen.galimulator.Star getNearestStar(snoddasmannen.galimulator.QuadTree argument) {
        throw new AssertionError();
    }

    @Overwrite
    public final Vector<Star> getStoredStars() {
        return new Vector<>(this.faststar$getStarsFast());
    }

    @Shadow
    private final float getWidth() {
        throw new AssertionError();
    }
}
