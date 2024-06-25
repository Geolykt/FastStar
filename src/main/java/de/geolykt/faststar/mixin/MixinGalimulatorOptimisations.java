/*
 * Copyright 2021 Geolykt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.geolykt.faststar.mixin;

import java.util.Vector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import snoddasmannen.galimulator.ax;
import snoddasmannen.galimulator.le;
import snoddasmannen.galimulator.nd;

@Mixin(le.class)
public class MixinGalimulatorOptimisations {

    @SuppressWarnings("rawtypes")
    @Shadow
    public static Vector a;

    @Shadow
    public static ax w;

    @Overwrite
    public static void a(double x, double y, double radius) {
        // We are changing the math to avoid using the expensive square-root operation by
        // using the square distance instead of the non-square distance
        radius *= radius;
        for (Object obj : a) {
            if (((nd)obj).b((float)x, (float)y) < radius) {
                ((nd)obj).b(w);
            }
        }
    }

}
