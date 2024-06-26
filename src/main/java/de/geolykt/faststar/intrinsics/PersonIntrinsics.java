package de.geolykt.faststar.intrinsics;

import org.jetbrains.annotations.NotNull;

import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.Job;
import snoddasmannen.galimulator.Person;
import snoddasmannen.galimulator.PersonSpecial;
import snoddasmannen.galimulator.Space;

public class PersonIntrinsics {

    public static int faststar$getJobScoreStandard(@NotNull Person person, @NotNull Job job) {
        float distSq = person.getJob().getEmployer().getCoordinates().dst2(job.getEmployer().getCoordinates());
        float totalHalfWidthSq = (Space.getMaxX() * Space.getMaxX()) / 4F;

        if (distSq > totalHalfWidthSq) {
            return 0;
        }

        float baseDesire = 1F;

        for (PersonSpecial special : person.getSpecials()) {
            baseDesire *= special.getJobDesireFactor(person, job);
        }

        if (baseDesire == 0F) {
            return 0;
        }

        float merit = person.getPrestige() + person.getAttributeValue(person.getJob().getEmployer().getMeritingAttribute()) * 10000;
        merit /= 1F + (distSq / totalHalfWidthSq);

        if (person.getJob().getEmployer() == job.getEmployer()) {
            merit *= 1.5F;
        } else if (person.getJob().getEmployer().getJobEmpire() == job.getEmployer().getJobEmpire()) {
            merit *= 1.2F;
        }

        if (EmploymentAgency.getInstance().getEmployingFamily(job.getEmployer()) == person.getFamily()) {
            merit *= 2.0F;
        }

        return (int) (merit * baseDesire);
    }
}
