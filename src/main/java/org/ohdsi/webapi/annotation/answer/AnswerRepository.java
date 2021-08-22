package org.ohdsi.webapi.annotation.answer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    public Answer findById(int answerId);
}
