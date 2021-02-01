package org.ohdsi.webapi.annotation.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ohdsi.webapi.annotation.question.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {}
