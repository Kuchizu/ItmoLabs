package io.github.Lab3.db;

import io.github.Lab3.model.CheckAreaBean;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class CheckAreaDAOImpl implements CheckAreaDAO {
    @Override
    public void addNewResult(CheckAreaBean result) throws SQLException {
        Session session = null;
        try {
            session = HibernateUtils.getFactory().openSession();
            session.beginTransaction();
            session.persist(result);
            session.getTransaction().commit();
        } catch (Throwable e) {
            System.err.println("Something went wrong in DAO: " + e);
            throw new SQLException(e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void updateResult(Long bus_id, CheckAreaBean result) throws SQLException {
        Session session = null;
        try {
            session = HibernateUtils.getFactory().openSession();
            session.beginTransaction();
            session.merge(result);
            session.getTransaction().commit();
        } catch (Throwable e) {
            System.err.println("Something went wrong in DAO: " + e);
            throw new SQLException(e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public CheckAreaBean getResultById(Long result_id) throws SQLException {
        Session session = null;
        CheckAreaBean result;
        try {
            session = HibernateUtils.getFactory().openSession();
            result = session.getReference(CheckAreaBean.class, result_id);
        } catch (Throwable e) {
            System.err.println("Something went wrong in DAO: " + e);
            throw new SQLException(e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    @Override
    public Collection<CheckAreaBean> getAllResults() throws SQLException {
        Session session = null;
        List<CheckAreaBean> results;
        try {
            session = HibernateUtils.getFactory().openSession();
            var criteriaQuery = session.getCriteriaBuilder().createQuery(CheckAreaBean.class);
            Root<CheckAreaBean> root = criteriaQuery.from(CheckAreaBean.class);
            results = session.createQuery(criteriaQuery.select(root)).getResultList();
        } catch (Throwable e) {
            System.err.println("Something went wrong in DAO: " + e);
            throw new SQLException(e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return results;
    }

    @Override
    public void deleteResult(CheckAreaBean result) throws SQLException {
        Session session = null;
        try {
            session = HibernateUtils.getFactory().openSession();
            session.beginTransaction();
            session.remove(result);
            session.getTransaction().commit();
        } catch (Throwable e) {
            System.err.println("Something went wrong in DAO: " + e);
            throw new SQLException(e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public final String TABLE_NAME = "results";
    @Override
    public void clearResults() throws SQLException {
        Session session = null;
        try {
            session = HibernateUtils.getFactory().openSession();
            session.beginTransaction();
            String sql = "DELETE FROM " + TABLE_NAME;
            session.createNativeQuery(sql, this.getClass()).executeUpdate();
            session.getTransaction().commit();
        } catch (Throwable e) {
            System.err.println("Something went wrong in DAO: " + e);
            throw new SQLException(e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
