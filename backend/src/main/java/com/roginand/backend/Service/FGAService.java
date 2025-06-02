package com.roginand.backend.Service;

import com.roginand.backend.DTO.Student;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FGAService {
    private final FGAAlgo fgaAlgo = new FGAAlgo();

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
}
