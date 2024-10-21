package de.geolykt.faststar.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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
import org.stianloader.stianknn.PointObjectPair;
import org.stianloader.stianknn.SpatialIndexKNN;
import org.stianloader.stianknn.SpatialQueryArray;

import com.badlogic.gdx.math.Vector2;

import de.geolykt.faststar.intrinsics.EmploymentAgencyExpress.EmploymentAgencySnailAccess;
import de.geolykt.faststar.intrinsics.SetBasedPseudoList;
import de.geolykt.starloader.api.Galimulator;

import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.Job;
import snoddasmannen.galimulator.Person;

@Mixin(value = EmploymentAgency.class, priority = 8000)
public class FastEmploymentAgencyMixins implements EmploymentAgencySnailAccess {

    @Shadow
    private static EmploymentAgency instance;

    @Shadow
    transient ExecutorService b;

    @Unique
    private transient SpatialIndexKNN<@NotNull Job>[] faststar$jobsAtLevel;

    @Shadow
    private List<Job>[] jobsPerLevel;

    @Shadow
    private List<Job> openings;

    @Shadow
    private List<Person>[] personsPerLevel;

    @Shadow
    private Person a(Job job, int integer) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Overwrite
    private void clearPeoplePerLevel() {
        this.personsPerLevel = new List[5];

        for (int var1 = 0; var1 < 5; var1++) {
            this.personsPerLevel[var1] = new SetBasedPseudoList<>(new LinkedHashSet<>());
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void faststar$assembleQueryArrays(CallbackInfo ci) {
        // Currently the lists stored in 'personsPerLevel' are not thread-safe, however once we find a solution
        // for that small issue we will try to do the query array assembly asynchronously if possible
        // Do note that we need to do it in sync for the first tick of a galaxy.

        // For the meantime, we will further reduce the frequency in which the arrays are assembled.
        if (this.faststar$jobsAtLevel != null && Galimulator.getGameYear() % 60 != 0) {
            return;
        }

        this.faststar$assembleQueryArraysSync();
    }

    public void faststar$assembleQueryArraysSync() {
        List<Job>[] jobsPerLevel = this.jobsPerLevel;
        @SuppressWarnings("unchecked") // Array initialisations with generics are not a thing - thank you JLS developers!
        SpatialQueryArray<@NotNull Job>[] jobsAtLevelQueries = new SpatialQueryArray[jobsPerLevel.length];
        for (int i = 0; i < jobsPerLevel.length; i++) {
            List<Job> jobs = jobsPerLevel[i];
            Collection<PointObjectPair<Job>> peoplePosition = new ArrayList<>();
            for (Job job : jobs) {
                Vector2 location = job.getEmployer().getCoordinates();
                peoplePosition.add(new PointObjectPair<>(job, location.x, location.y));
            }

            @SuppressWarnings("deprecation")
            float minX = Galimulator.getMap().getWidth() * -0.6F;
            float maxX = -minX;
            @SuppressWarnings("deprecation")
            float minY = Galimulator.getMap().getHeight() * -0.6F;
            float maxY = -minY;
            float gridW = 16 * 0.035F;
            float gridH = 16 * 0.035F;
            jobsAtLevelQueries[i] = new SpatialQueryArray<>(peoplePosition, minX, minY, maxX, maxY, gridW, gridH);
        }

        this.faststar$jobsAtLevel = jobsAtLevelQueries;
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
        this.faststar$jobsAtLevel[level - 1].queryKnn(location.x, location.y, 40, (nearbyJob) -> {
            Person nearbyPerson = nearbyJob.getCurrentHolder();
            if (nearbyPerson != null) {
                candidates.add(nearbyPerson);
            }
        });

        candidates.remove(replacement);

        candidates.removeIf(p -> {
            Job oldJob = p.getJob();

            // The person must be currently employed (and thus be alive)
            if (oldJob == null) {
                return true;
            }

            // Only allow job changes if they are promotions
            return oldJob.getJobLevel() >= job.getJobLevel();
        });

        return candidates;
    }

    @Override
    public Person faststar$invokeFindReplacement(Job vacantJob, int replacementRank) {
        return this.a(vacantJob, replacementRank);
    }

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void faststar$linkedOpenings(CallbackInfo ci) {
        if (this.openings instanceof ArrayList) {
            this.openings = new LinkedList<>(this.openings);
        }
    }
}
