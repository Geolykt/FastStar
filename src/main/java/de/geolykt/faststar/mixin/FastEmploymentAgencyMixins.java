package de.geolykt.faststar.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.math.Vector2;

import de.geolykt.faststar.intrinsics.SetBasedPseudoList;
import de.geolykt.faststar.intrinsics.SpatialQueryArray;
import de.geolykt.faststar.intrinsics.SpatialQueryArray.PointObjectPair;
import de.geolykt.starloader.api.Galimulator;

import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.Job;
import snoddasmannen.galimulator.Person;

@Mixin(value = EmploymentAgency.class, priority = 8000)
public class FastEmploymentAgencyMixins {

    @Unique
    private transient SpatialQueryArray<Person>[] faststar$peopleAtLevel;

    @Shadow
    private List<Person>[] personsPerLevel;

    @SuppressWarnings("unchecked")
    @Overwrite
    private void clearPeoplePerLevel() {
        this.personsPerLevel = new List[5];

        for (int var1 = 0; var1 < 5; var1++) {
            this.personsPerLevel[var1] = new SetBasedPseudoList<>(ConcurrentHashMap.newKeySet());
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void faststar$assembleQueryArrays(CallbackInfo ci) {
        // Currently the lists stored in 'personsPerLevel' are not thread-safe, however once we find a solution
        // for that small issue we will try to do the query array assembly asynchronously if possible
        // Do note that we need to do it in sync for the first tick of a galaxy.

        // For the meantime, we will further reduce the frequency in which the arrays are assembled.
        if (this.faststar$peopleAtLevel != null && Galimulator.getGameYear() % 60 != 0) {
            return;
        }

        this.faststar$assembleQueryArraysSync();
    }

    @Unique
    private void faststar$assembleQueryArraysSync() {
        List<Person>[] peopleAtLevel = this.personsPerLevel;
        @SuppressWarnings("unchecked") // Array initialisations with generics are not a thing - thank you JLS developers!
        SpatialQueryArray<Person>[] peopleAtLevelQueries = new SpatialQueryArray[peopleAtLevel.length];
        for (int i = 0; i < peopleAtLevel.length; i++) {
            List<Person> people = peopleAtLevel[i];
            Collection<PointObjectPair<Person>> peoplePosition = new ArrayList<>();
            for (Person person : people) {
                Vector2 location = person.getJob().getEmployer().getCoordinates();
                peoplePosition.add(new PointObjectPair<>(person, location.x, location.y));
            }
            peopleAtLevelQueries[i] = new SpatialQueryArray<>(peoplePosition);
        }

        this.faststar$peopleAtLevel = peopleAtLevelQueries;
    }

    /**
     * By default SLAPI (which basically copies galimulator code), will consider any
     * person at the requested level a candidate and then will compute their desire
     * for the job. At medium-sized galaxies (5k stars) this can already go into the
     * tens of thousands of people, however the amount and frequency in which the
     * computation needs to occur increases with the size of the galaxy, quickly
     * bringing performance to a halt even though the game runs asynchronously.
     *
     * <p>Henceforth, FastStar will alter this behaviour to only choose candidates
     * which realistically are most likely to be chosen anyways - meaning family
     * members and nearby people. The proximity of a person is performed using a
     * k-nearest-neighbours query on a {@link SpatialQueryArray}.
     *
     * @param receiver The receiver - corresponds to <code>this</code> and as such ignored,
     * but required for the redirect capture.
     * @param level The level the people should be at
     * @param job The job for which a replacement should be searched for, captured from
     * the arguments of the caller methods
     * @return Likely candidates being at the requested job level for the given opening
     */
    @Redirect(
        target = @Desc(value = "a", args = {Job.class, int.class}, ret = Person.class),
        at = @At(value = "INVOKE", desc = @Desc(owner = EmploymentAgency.class, value = "getPeopleAtLevel", args = int.class, ret = List.class)),
        require = 2,
        allow = 2
    )
    private List<Person> faststar$getNearbyPeopleAtLevel(EmploymentAgency receiver, int level, @NotNull Job job) {
        Person replacement = job.getPreviousHolder();
        List<Person> members;
        if (replacement != null) {
            members = replacement.getFamily().getMembers();
        } else {
            members = Collections.emptyList();
        }

        List<Person> candidates = new ArrayList<>();
        for (Person familyMember : members) {
            if (familyMember != replacement && familyMember.getJob().getJobLevel() == level) {
                candidates.add(familyMember);
            }
        }

        Vector2 location = job.getEmployer().getCoordinates();
        this.faststar$peopleAtLevel[level - 1].queryKnn(location.x, location.y, 200, candidates);

        candidates.remove(replacement);

        if (candidates.isEmpty()) {
            candidates.addAll(this.personsPerLevel[level - 1]);
            return candidates;
        }

        candidates.removeIf(p -> {
            Job oldJob = p.getJob();

            // The person must be currently employed (and thus be alive)
            if (oldJob == null) {
                return true;
            }

            // Only allow job changes if they are promotions
            return oldJob.getJobLevel() == job.getJobLevel();
        });

        return candidates;
    }
}
