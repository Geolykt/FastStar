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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import snoddasmannen.galimulator.av;
import snoddasmannen.galimulator.aw;

@Mixin(value = av.class)
// The mixin removes the implementation of the timings class as they appear to have a very large impact on the performance of the game
public class RemoveTimingsMixin {

    @Overwrite
    public static void a(String var0, boolean var1, aw var2) {
        // We knowingly remove the implementation of this class as it's original implementation took time to compute 
        // and due to the large amount of calls to this method, this stacks up to a lot
    }

    @Overwrite
    public static int b(String var0, boolean var1) {
        return 0; // The map is no longer fed information through it - so we can take a shortcut
    }

    @Overwrite
    public static void a(aw var0) {
        // that too
    }

    @Overwrite
    public static void d() {
        // This originally iterated over the map, but because this is now no longer populated, this method is pretty much useless
    }
}
