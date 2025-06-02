package com.roginand.backend.Service;

import com.roginand.backend.DTO.Student;

import java.util.*;

public class FGAAlgo {
    TreeSet<Student> studentWithGrade = new TreeSet<>(new Comparator<Student>() {
        @Override
        public int compare(Student s1, Student s2) {
            int gradeComparison = Double.compare(s1.getGrade(), s2.getGrade());
            if (gradeComparison == 0) {
                return s1.getName().compareTo(s2.getName());
            }
            return gradeComparison;
        }
    });


    public void add(Student student_param) {
        if (student_param.getGrade() > 5.0 || student_param.getGrade() < 1.0) {
            System.out.println("Skipping student " + student_param.getName() + " with invalid grade: " + student_param.getGrade());
            return;
        }
        studentWithGrade.add(student_param);
    }

    public void print() {
        for (Student st : studentWithGrade) {
            System.out.println(st.getName() + " | " + st.getGrade());
        }
    }

    public TreeSet<Student> convertListToSet(List<Student> group) {
        studentWithGrade.addAll(group);

        return studentWithGrade;
    }

    public List<List<Student>> assignGroup(int groupAmount) {
        int totalStudents = studentWithGrade.size();

        if (totalStudents < groupAmount)    {
            System.out.println("Not enough students to form " + groupAmount + " groups (minimum 1 student per group).");
            return null;
        }

        // Initialize groups
        List<List<Student>> groups = new ArrayList<>();
        for (int i = 0; i < groupAmount; i++) {
            groups.add(new ArrayList<>());
        }

        // Round-robin assignment
        LinkedList<Student> sortedStudentsDeque = new LinkedList<>(studentWithGrade);
        int baseStudentsPerGroup = totalStudents / groupAmount;
        int remainder = totalStudents % groupAmount;
        int[] groupSizes = new int[groupAmount];
        for (int i = 0; i < groupAmount; i++) {
            groupSizes[i] = baseStudentsPerGroup + (i < remainder ? 1 : 0);
        }


        while (!sortedStudentsDeque.isEmpty()) {
            for (int groupIndex = 0; groupIndex < groupAmount; groupIndex++) {
                if (groups.get(groupIndex).size() < groupSizes[groupIndex]) {
                    Student student = sortedStudentsDeque.pollFirst();
                    groups.get(groupIndex).add(student);
                }
            }
        }

        // Swapping phase using divide-and-conquer
        balanceGroupsDivideConquer(groups, 0, groupAmount - 1);

        return groups;
    }


    private void balanceGroupsDivideConquer(List<List<Student>> groups, int left, int right) {
        if (left >= right) {
            return;
        }

        int mid = left + (right - left) / 2;
        balanceGroupsDivideConquer(groups, left, mid);
        balanceGroupsDivideConquer(groups, mid + 1, right);

        mergeAndBalance(groups, left,  right);

    }


    private void mergeAndBalance(List<List<Student>> groups, int left, int right) {
        boolean swapped;

        if (left == right)
            return;

        do {
            swapped = false;
            int maxIndex = -1, minIndex = -1;
            double maxAvg = Double.MIN_VALUE, minAvg = Double.MAX_VALUE;

            for (int i = left; i <= right; i++) {
                double avg = calculateGroupAverage(groups.get(i));
                if (avg > maxAvg) {
                    maxAvg = avg;
                    maxIndex = i;
                }
                if (avg < minAvg) {
                    minAvg = avg;
                    minIndex = i;
                }
            }

            if (maxIndex != -1 && minIndex != -1) {
                if (improveBySwapping(groups, maxIndex, minIndex))
                    swapped = true;
            }
        } while (swapped);
    }


    public double calculateGroupAverage(List<Student> group) {
        if (group.isEmpty()) return 0;
        double sum = 0;
        for (Student s : group) {
            sum += s.getGrade();
        }
        return (double) Math.round(sum / group.size() * 10) / 10;
    }


    private boolean improveBySwapping(List<List<Student>> groups, int group1Index, int group2Index) {
        List<Student> group1 = groups.get(group1Index);
        List<Student> group2 = groups.get(group2Index);
        double avg1 = calculateGroupAverage(group1);
        double avg2 = calculateGroupAverage(group2);
        double initialDiff = Math.abs(avg1 - avg2);


        double bestImprovement = 0;
        Student bestStudent1 = null;
        Student bestStudent2 = null;

        int i = 0, j = 0;
        while (i < group1.size() && j < group2.size()) {
            Student s1 = group1.get(i);
            Student s2 = group2.get(j);
            double newAvg1 = (avg1 * group1.size() - s1.getGrade() + s2.getGrade()) / group1.size();
            double newAvg2 = (avg2 * group2.size() - s2.getGrade() + s1.getGrade()) / group2.size();
            double newDiff = Math.abs(newAvg1 - newAvg2);
            double improvement = initialDiff - newDiff;

            if (newDiff < initialDiff && improvement > bestImprovement) {
                bestImprovement = improvement;
                bestStudent1 = s1;
                bestStudent2 = s2;
            }
            if (s1.getGrade() < s2.getGrade())
                i++;
            else
                j++;

        }
        if (bestStudent1 != null && bestStudent2 != null) {
            group1.remove(bestStudent1);
            group2.remove(bestStudent2);
            group1.add(bestStudent2);
            group2.add(bestStudent1);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Student s : studentWithGrade) {
            sb.append(s.getName()).append(" (").append(s.getGrade()).append(")").append("\n");
        }

        return sb.toString();
    }
}
