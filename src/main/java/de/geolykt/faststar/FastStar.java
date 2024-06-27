package de.geolykt.faststar;

import de.geolykt.faststar.intrinsics.PersonIntrinsics;
import de.geolykt.faststar.intrinsics.SpatialQuery;
import de.geolykt.starloader.api.event.EventHandler;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.Listener;
import de.geolykt.starloader.api.event.lifecycle.GraphicalTickEvent;
import de.geolykt.starloader.api.event.lifecycle.LogicalTickEvent;
import de.geolykt.starloader.api.event.lifecycle.LogicalTickEvent.Phase;
import de.geolykt.starloader.mod.Extension;

import snoddasmannen.galimulator.Space;

public class FastStar extends Extension {

    public static final float GRANULARITY_FACTOR = 0.035F;

    @Override
    public void initialize() {
        EventManager.registerListener(new Listener() {
            @EventHandler
            public void onGraphicalTick(GraphicalTickEvent event) {
                // FIXME something is fishy here (LogicalTickEvent does not work??)
                if (event.getPhase() == GraphicalTickEvent.Phase.PRE) {
                    SpatialQuery.updateStarsActorDrawing();
                }
            }

            @EventHandler
            public void onLogicalTick(LogicalTickEvent event) {
                if (event.getPhase() == Phase.PRE_LOGICAL) {
                    PersonIntrinsics.jobSeekRangeSq = Math.min((Space.getMaxX() * Space.getMaxX()) / 4F, FastStar.GRANULARITY_FACTOR * 200F);
                }
            }
        });
    }

    @Override
    public void terminate() {}
}
