package com.ssp.table.questions;

import org.springframework.jdbc.core.*;

import java.sql.*;

/**
 * mapper для вопросов, чтобы вытащить с БД
 */
public class QuestionMapper implements RowMapper<QuestionInfo> {

    public static final String BASE_SQL
            = "Select ba.Id, ba.Question, ba.Meaning From Questions ba Order By ba.Id ";

    @Override
    public QuestionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {

        Long id = rs.getLong("Id");
        String question = rs.getString("Question");
        Long meaning = rs.getLong("Meaning");

        return new QuestionInfo(id, question,"",meaning);
    }

}
