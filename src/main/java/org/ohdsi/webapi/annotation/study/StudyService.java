package org.ohdsi.webapi.annotation.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StudyService {
    @Autowired
    private StudyRepository studyRepository;

    public void addStudy(Study study) {
        studyRepository.save(study);
    }
}
