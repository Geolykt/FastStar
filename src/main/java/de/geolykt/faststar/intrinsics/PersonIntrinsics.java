package de.geolykt.faststar.intrinsics;

import org.jetbrains.annotations.NotNull;

import snoddasmannen.galimulator.Employer;
import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.Job;
import snoddasmannen.galimulator.Person;
import snoddasmannen.galimulator.PersonSpecial;
import snoddasmannen.galimulator.Space;

public class PersonIntrinsics {

    public static float jobSeekRangeSq = (Space.getMaxX() * Space.getMaxX()) / 4F;

    public static int faststar$getJobScoreStandard(@NotNull Person person, @NotNull Job job) {
        Employer currentEmployer = person.getJob().getEmployer();
        Employer prospectiveEmployer = job.getEmployer();
        float distSq = currentEmployer.getCoordinates().dst2(prospectiveEmployer.getCoordinates());
        float jobSeekRangeSq = PersonIntrinsics.jobSeekRangeSq;

        if (distSq > jobSeekRangeSq) {
            return 0;
        }

        float baseDesire = 1F;

        for (PersonSpecial special : person.getSpecials()) {
            baseDesire *= special.getJobDesireFactor(person, job);
        }

        if (baseDesire == 0F) {
            return 0;
        }

        float merit = person.getPrestige() + person.getAttributeValue(currentEmployer.getMeritingAttribute()) * 10000;
        merit /= 1F + (distSq / jobSeekRangeSq);

        if (currentEmployer == prospectiveEmployer) {
            merit *= 1.5F;
        } else if (currentEmployer.getJobEmpire() == prospectiveEmployer.getJobEmpire()) {
            merit *= 1.2F;
        }

        if (EmploymentAgency.getInstance().getEmployingFamily(prospectiveEmployer) == person.getFamily()) {
            merit *= 2.0F;
        }

        return (int) (merit * baseDesire);
    }
}
