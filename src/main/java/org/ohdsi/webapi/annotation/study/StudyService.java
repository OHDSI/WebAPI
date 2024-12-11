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
    public Study getStudyById(int studyId){
        return studyRepository.findByStudyId(studyId);
    }
    public Study getStudyByQuestionSetIdAndSampleId(int questionSetId, int sampleId){
        return studyRepository.findByQuestionSetIdAndSampleId(questionSetId,sampleId);
    }
}
