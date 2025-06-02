package com.roginand.backend.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.roginand.backend.DTO.GroupResult;
import com.roginand.backend.DTO.Student;
import com.roginand.backend.Service.FGAService;

@RestController
@RequestMapping("/api")
public class FGAController {
    FGAService fgaService = new FGAService();

    @PostMapping("/assign/{group}")
    public ResponseEntity<List<GroupResult>> addStudents(@RequestBody List<Student> student, @PathVariable int group) {
        StringBuilder sb = new StringBuilder();

        fgaService.convertListToSet(student);

        return getListResponseEntity(group);
    }

    @PostMapping(value = "/assign-csv/{group}", consumes = "text/csv")
    public ResponseEntity<List<GroupResult>> addStudentsFromCsv(@RequestBody String csvData, @PathVariable int group) throws IOException {
        List<Student> students = parseCsvToStudents(csvData);

        for (Student s : students) {
            fgaService.addStudent(s);
        }
        return getListResponseEntity(group);
    }

    @GetMapping("/")
    public String hello() {
        return "This is the backend for FairGroupAssignment";
    }
    
    private ResponseEntity<List<GroupResult>> getListResponseEntity(@PathVariable int group) {
        List<List<Student>> groups = fgaService.assignGroups(group);

        List<GroupResult> results = new ArrayList<>();

        for (int i = 0; i < groups.size(); i++) {
            List<Student> studentList = groups.get(i);
            Collections.sort(studentList);
            double avg = fgaService.calculateGroupAverage(studentList);

            GroupResult groupResult = new GroupResult(i + 1, avg, studentList);
            results.add(groupResult);
        }

        return ResponseEntity.ok(results);
    }

    private List<Student> parseCsvToStudents(String csvData) throws IOException {
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csvData))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines or headers if you want
                if (line.trim().isEmpty() || line.startsWith("name")) continue;

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim();
                    double grade = Double.parseDouble(parts[1].trim());
                    students.add(new Student(name, grade));
                }
            }
        }
        return students;
    }

}
