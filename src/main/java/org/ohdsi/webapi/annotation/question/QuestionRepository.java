package org.ohdsi.webapi.annotation.question;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.ohdsi.webapi.annotation.question.Question;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    public Question findById(int questionId);
}
