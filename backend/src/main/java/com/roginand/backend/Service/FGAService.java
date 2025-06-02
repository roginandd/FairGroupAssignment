package com.roginand.backend.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.roginand.backend.DTO.Student;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FGAService {
    private final FGAAlgo fgaAlgo;

    public void addStudent(Student student) {
        fgaAlgo.add(student);
    }

    public List<List<Student>> assignGroups(int groupCount) {
        return fgaAlgo.assignGroup(groupCount);
    }

    public double calculateGroupAverage(List<Student> group) {
        return fgaAlgo.calculateGroupAverage(group);
    }

    public void convertListToSet(List<Student> group) {
        fgaAlgo.convertListToSet(group);
    }

    public void clearStudent() {
        fgaAlgo.clear();
    }
}
