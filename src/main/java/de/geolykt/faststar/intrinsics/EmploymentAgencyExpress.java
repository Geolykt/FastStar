package de.geolykt.faststar.intrinsics;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.gui.BackgroundTask;

import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.Job;
import snoddasmannen.galimulator.Person;
import snoddasmannen.galimulator.PersonSpecial;
import snoddasmannen.galimulator.Settings.EnumSettings;
import snoddasmannen.galimulator.Space;

public class EmploymentAgencyExpress {

    public static interface EmploymentAgencySnailAccess {
        void faststar$assembleQueryArraysSync();
        Person faststar$invokeFindReplacement(Job vacantJob, int replacementRank);
    }

    private static class PopulationTask implements BackgroundTask {

        volatile int index;
        int totalJobs;

        @Override
        @NotNull
        public String getProgressDescription() {
            NumberFormat format = NumberFormat.getPercentInstance();
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            return "EmploymentAgencyExpress: Populating Galaxy: " + format.format(this.getTaskProgress()) + " done.";
        }

        @Override
        public float getTaskProgress() {
            return ((float) this.index) / this.totalJobs;
        }
    }

    private static final boolean EXPRESS_PERSON_CREATION = true;

    private static ConcurrentNavigableMap<Integer, Person>[] peopleGrid;

    private static Person createPersonExpress(Vector2 position, float maxX, float maxY) {
        if (!EXPRESS_PERSON_CREATION) {
            return Space.c(position);
        }

        float x = MathUtils.clamp(position.x, -maxX, maxX);
        float y = MathUtils.clamp(position.y, -maxX, maxX);

        int idx = (int) ((x + maxX) / 0.35F) + (int) ((y + maxY) / 0.35F) * ((int) Math.ceil(maxX * 2F));
        ConcurrentNavigableMap<Integer, Person> gridSquare = EmploymentAgencyExpress.peopleGrid[idx];
        Map.Entry<Integer, Person> pe1 = gridSquare.lowerEntry(ThreadLocalRandom.current().nextInt() & 0x7FFF_FFFF);
        Map.Entry<Integer, Person> pe2 = gridSquare.lowerEntry(ThreadLocalRandom.current().nextInt() & 0x7FFF_FFFF);

        Person p;
        if (pe1 != pe2 && pe1 != null && pe2 != null) {
            Person p1 = pe1.getValue();
            Person p2 = pe2.getValue();
            if (p1.getAlive() && p2.getAlive()
                    && p1.getChildCount() <= 8 && p2.getChildCount() <= 8
                    && !p2.hasSpecial(PersonSpecial.CHILD) && !p2.hasSpecial(PersonSpecial.CHILD)) {
                p = new Person(p1, p2);
            } else {
                p = new Person(null);
            }
        } else {
            p = new Person(null);
        }

        gridSquare.put(p.id, p);
        Space.getPersons().add(p);
        return p;
    }

    public static void populateGalaxy(EmploymentAgency employmentAgency) {
        if (!((Boolean) EnumSettings.DYNASTY_MODE.getValue())) {
            return;
        }

        float maxX = Space.getMaxX() + 0.7F;
        float maxY = Space.getMaxY() + 0.7F;
        @SuppressWarnings("unchecked")
        ConcurrentNavigableMap<Integer, Person>[] peopleGrid = new ConcurrentNavigableMap[(int) ((maxY * 2) / 0.35F) * ((int) Math.ceil(maxX * 2F))];
        EmploymentAgencyExpress.peopleGrid = peopleGrid;
        for (int i = 0; i < peopleGrid.length; i++) {
            peopleGrid[i] = new ConcurrentSkipListMap<>();
        }

        EmploymentAgencySnailAccess snailAccess = (EmploymentAgencySnailAccess) employmentAgency;
        @SuppressWarnings("unchecked")
        List<Job>[] jobs = employmentAgency.jobsPerLevel;
        BackgroundTask previousTask = Galimulator.getBackgroundTask();
        PopulationTask task = new PopulationTask();
        task.totalJobs = 0;

        // build estimate
        for (int i = 0; i < jobs.length; i++) {
            task.totalJobs += (i + 1) * jobs[i].size();
        }

        Galimulator.setBackgroundTask(task);

        snailAccess.faststar$assembleQueryArraysSync();

        @SuppressWarnings("unchecked")
        List<Job>[] borrowedJobs = new List[jobs.length - 1];
        for (int i = 0; i < borrowedJobs.length; i++) {
            borrowedJobs[i] = new ArrayList<>();
        }

        // Our goal is to fill each and every job anyways.
        // Further, we keep track of open jobs in a different manner that is more performance friendly.
        // So you might ask, why is this method faster? Well quite easy: List#remove(Object) is a very
        // costly operation on very large lists (which is the case here), so by having an empty list,
        // the operation is nearly a NOP.
        employmentAgency.openings.clear();
        @SuppressWarnings("unchecked")
        List<Job> openingsOriginal = employmentAgency.openings;
        employmentAgency.openings = new NOPList<>();

        {
            List<Job> lowbornJobs = jobs[0];
            int lowborns = lowbornJobs.size();
            for (int i = 0; i < lowborns; i++) {
                Job job = lowbornJobs.get(task.index = i);
                employmentAgency.a(job, EmploymentAgencyExpress.createPersonExpress(job.getEmployer().getCoordinates(), maxX, maxY));
            }
        }

        for (int j = 1; j < jobs.length; j++) {
            List<Job> populatingJobs = jobs[j];
            int populatingCount = populatingJobs.size();
            for (int i = 0; i < populatingCount; i++) {
                Job job = populatingJobs.get(i);
                Person replacement = snailAccess.faststar$invokeFindReplacement(job, j);
                if (replacement == null) {
                    replacement = EmploymentAgencyExpress.createPersonExpress(job.getEmployer().getCoordinates(), maxX, maxY);
                    task.index += j + 1;
                } else {
                    Job borrowed = replacement.getJob();
                    borrowedJobs[borrowed.getJobLevel()].add(borrowed);
                    task.index += (borrowed.getJobLevel() - j + 1);
                }
                employmentAgency.a(job, replacement);
            }

            // Bubble down the borrowed jobs
            for (int i = j - 1; i > 0; i--) {
                List<Job> bubbleHost = borrowedJobs[i];
                int bubbleHosts = bubbleHost.size();
                for (int k = 0; k < bubbleHosts; k++) {
                    Job job = bubbleHost.get(k);
                    Person replacement = snailAccess.faststar$invokeFindReplacement(job, i);
                    if (replacement == null) {
                        replacement = EmploymentAgencyExpress.createPersonExpress(job.getEmployer().getCoordinates(), maxX, maxY);
                        task.index += i;
                    } else {
                        Job borrowed = replacement.getJob();
                        borrowedJobs[borrowed.getJobLevel()].add(borrowed);
                        task.index += (borrowed.getJobLevel() - i + 1);
                    }
                    employmentAgency.a(job, replacement);
                }
                bubbleHost.clear();
            }

            // Repopulate borrowed lowborns
            List<Job> lowbornJobs = borrowedJobs[0];
            int lowborns = lowbornJobs.size();
            for (int i = 0; i < lowborns; i++) {
                Job job = lowbornJobs.get(i);
                employmentAgency.a(job, EmploymentAgencyExpress.createPersonExpress(job.getEmployer().getCoordinates(), maxX, maxY));
                task.index++;
            }
            lowbornJobs.clear();
        }

        employmentAgency.openings = openingsOriginal;
        Galimulator.setBackgroundTask(previousTask);
        employmentAgency.tick();
    }
}
