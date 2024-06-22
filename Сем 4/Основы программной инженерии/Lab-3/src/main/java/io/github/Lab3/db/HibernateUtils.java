package io.github.Lab3.db;

import io.github.Lab3.model.CheckAreaBean;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class HibernateUtils {
    private static final SessionFactory factory;

    static {
        try {
            Properties info = new Properties();
            info.load(HibernateUtils.class.getClassLoader().getResourceAsStream("/db.cfg"));
            factory = new Configuration().configure()
                    .setProperty(AvailableSettings.USER,
                            info.getProperty("user"))
                    .setProperty(AvailableSettings.PASS,
                            info.getProperty("password"))
                    .addAnnotatedClass(CheckAreaBean.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Something went wrong during initializing Hibernate: " + ex);
            throw new ExceptionInInitializerError();
        }
    }

    public static SessionFactory getFactory() {
        return factory;
    }
}
