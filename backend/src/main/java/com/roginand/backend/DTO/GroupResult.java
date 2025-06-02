package com.roginand.backend.DTO;

import java.util.List;

public class GroupResult {
    private int groupNumber;
    private double averageGrade;
    private List<Student> students;

    public GroupResult(int groupNumber, double averageGrade, List<Student> students) {
        this.groupNumber = groupNumber;
        this.averageGrade = averageGrade;
        this.students = students;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public double getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(double averageGrade) {
        this.averageGrade = averageGrade;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
