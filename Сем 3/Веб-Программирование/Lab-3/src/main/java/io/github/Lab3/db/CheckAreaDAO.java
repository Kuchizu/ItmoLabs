package io.github.Lab3.db;

import io.github.Lab3.model.CheckAreaBean;

import java.sql.SQLException;
import java.util.Collection;

public interface CheckAreaDAO {
    void addNewResult(CheckAreaBean result) throws SQLException;
    void updateResult(Long bus_id, CheckAreaBean result) throws SQLException;
    CheckAreaBean getResultById(Long result_id) throws SQLException;
    Collection<CheckAreaBean> getAllResults() throws SQLException;
    void deleteResult(CheckAreaBean result) throws SQLException;
    void clearResults() throws SQLException;
}
