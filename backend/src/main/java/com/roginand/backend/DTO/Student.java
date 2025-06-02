package com.roginand.backend.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Objects;

@Getter @Setter
public  class Student implements Comparable<Student>  {
    private String name;
    private double grade;


    public Student(String name, double grade) {
        this.name = name;
        this.grade = Math.round(grade * 10.0) / 10.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return name.equals(student.name);  // unique by name
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Student other) {
        return Double.compare(this.grade, other.grade);  // ascending
    }
}
