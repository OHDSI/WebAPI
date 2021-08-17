package org.ohdsi.webapi.annotation.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ohdsi.webapi.annotation.answer.Answer;
import org.springframework.data.jpa.repository.Query;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query("Select a FROM Answer a WHERE a.id = ?1")
    public Answer findByAnswerId(int answerId);
}
