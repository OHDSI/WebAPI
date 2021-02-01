package org.ohdsi.webapi.annotation.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ohdsi.webapi.annotation.answer.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {}
