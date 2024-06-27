package de.geolykt.faststar.mixin;

import org.spongepowered.asm.mixin.Mixin;

import snoddasmannen.galimulator.actors.Actor;

@Mixin(value = Actor.class, priority = 3000)
public class ActorMixins {

}
