package com.roginand.backend.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.roginand.backend.DTO.GroupResult;
import com.roginand.backend.DTO.Student;
import com.roginand.backend.Service.FGAService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("https://fair-group-assignment.vercel.app")
public class FGAController {
    private final FGAService fgaService;
    

    // For manual assigning
    @PostMapping("/assign/{groupCount}")
    public List<GroupResult> assignGroups(@RequestBody List<Student> students, @PathVariable int groupCount) {
        fgaService.convertListToSet(students);
        List<GroupResult> results = new ArrayList<>();
        List<List<Student>> groups = fgaService.assignGroups(groupCount);

        if (groups == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many groups for the number of students.");
        }

        for (int i = 0; i < groups.size(); i++) {
            List<Student> group = groups.get(i);    
            double average = group.stream()
                                  .mapToDouble(Student::getGrade)
                                  .average()
                                  .orElse(0);
            results.add(new GroupResult(i + 1, Math.round(average * 100) / 100.0, group));
        }
    
        return results;
    }
    

    // For CSV
    @PostMapping(value = "/assign-csv/{group}", consumes = "multipart/form-data")
    public ResponseEntity<?> assignCsv(@PathVariable int group, @RequestParam("file") MultipartFile file) throws IOException {
        String csvData = new String(file.getBytes());
        List<Student> students = parseCsvToStudents(csvData);
    
        for (Student s : students) {
            fgaService.addStudent(s);
        }
        return getListResponseEntity(group);
    }

    
    private ResponseEntity<?> getListResponseEntity(@PathVariable int group) {
        List<List<Student>> groups = fgaService.assignGroups(group);
        GroupResult groupResult = null;
        
        if (group <= 0 || group > groups.size()) {
            throw new IllegalArgumentException("Group number must be between 1 and number of students");
        }
        
        List<GroupResult> results = new ArrayList<>();

        for (int i = 0; i < groups.size(); i++) {
            List<Student> studentList = groups.get(i);
            double avg = fgaService.calculateGroupAverage(studentList);
            
            groupResult = new GroupResult(i, avg, studentList);

            results.add(groupResult);
        }

        return ResponseEntity.ok(groups);
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
