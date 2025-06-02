package com.roginand.backend.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

            if (group == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many groups for the number of students.");

            double average = group.stream()
                                  .mapToDouble(Student::getGrade)
                                  .average()
                                  .orElse(0);
            results.add(new GroupResult(i + 1, Math.round(average * 100) / 100.0, group));
        }
    
        return results;
    }

}
