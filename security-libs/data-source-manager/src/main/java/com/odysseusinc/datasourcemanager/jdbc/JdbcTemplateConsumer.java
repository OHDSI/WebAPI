package com.odysseusinc.datasourcemanager.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

@FunctionalInterface
public interface JdbcTemplateConsumer<T> {

	T execute(JdbcTemplate jdbcTemplate);
}
