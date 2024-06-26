package de.geolykt.faststar.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.faststar.intrinsics.PersonIntrinsics;

import snoddasmannen.galimulator.Job;
import snoddasmannen.galimulator.Person;

@Mixin(value = Person.class, priority = 3000)
public class PersonMixins {

    @Overwrite
    public int getJobDesire(@NotNull Job job) {
        if (job.getType() == Job.JobType.EMPEROR && job.getPreviousHolder() != null) {
            return this.a(job);
        } else {
            return PersonIntrinsics.faststar$getJobScoreStandard((Person) (Object) this, job);
        }
    }

    @Shadow
    public int a(@NotNull Job job) {
        return 0;
    }
}
