package de.geolykt.faststar.mixin;

import org.spongepowered.asm.mixin.Mixin;

import de.geolykt.faststar.AsynchronousAuxiliaryPanListener;
import de.geolykt.starloader.impl.gui.ForwardingListener;

@Mixin(ForwardingListener.class)
public class ForwardingListenerMixins implements AsynchronousAuxiliaryPanListener {
    // Nothing needs to be performed, this class only implements a marker interface
}
