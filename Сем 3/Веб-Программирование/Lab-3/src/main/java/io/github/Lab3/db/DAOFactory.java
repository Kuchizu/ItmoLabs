package io.github.Lab3.db;

public class DAOFactory {
    private static CheckAreaDAO resultDAO;

    private static DAOFactory instance;

    public static DAOFactory getInstance() {
        if (instance == null)
            instance = new DAOFactory();
        return instance;
    }
    
    public CheckAreaDAO getResultDAO() {
        if (resultDAO == null)
            resultDAO = new CheckAreaDAOImpl();
        return resultDAO;
    }
}
