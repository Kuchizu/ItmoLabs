package io.github.Lab3.utils;

import jakarta.servlet.ServletContextListener;
import lombok.experimental.UtilityClass;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class MBeanRegistry implements ServletContextListener {
  private static final Map<Class<?>, ObjectName> beans = new HashMap<>();

  public static void registerBean(Object bean, String name) {
    try {
      var domain = bean.getClass().getPackageName();
      var type = bean.getClass().getSimpleName();
      var objectName = new ObjectName(String.format("%s:type=%s,name=%s", domain, type, name));

      ManagementFactory.getPlatformMBeanServer().registerMBean(bean, objectName);
      beans.put(bean.getClass(), objectName);
    } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException |
             MalformedObjectNameException ex) {
      ex.printStackTrace();
    }
  }

  public static void unregisterBean(Object bean) {
    if (!beans.containsKey(bean.getClass())) {
      throw new IllegalArgumentException("Specified bean is not registered.");
    }

    try {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(beans.get(bean.getClass()));
    } catch (InstanceNotFoundException | MBeanRegistrationException ex) {
      ex.printStackTrace();
    }
  }
}
