package io.github.Lab3.repositories;

import io.github.Lab3.beans.AttemptStats;
import io.github.Lab3.beans.HitRatio;
import io.github.Lab3.model.CheckAreaBean;
import io.github.Lab3.utils.MBeanRegistry;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.Serializable;

@Named("attemptRepository")
@SessionScoped
public class AttemptRepository implements Serializable {
  private static final AttemptStats statsMBean = new AttemptStats();
  private static final HitRatio hitRatioMBean = new HitRatio();

  @PersistenceContext
  private EntityManager entityManager;

  public void init(@Observes @Initialized(SessionScoped.class) Object unused) {
    MBeanRegistry.registerBean(statsMBean, "attemptStats");
    MBeanRegistry.registerBean(hitRatioMBean, "hitRatio");
  }

  public void destroy(@Observes @Destroyed(SessionScoped.class) Object unused) {
    MBeanRegistry.unregisterBean(statsMBean);
    MBeanRegistry.unregisterBean(hitRatioMBean);
  }

  @Transactional
  public static void addAttempt(double x, double y, boolean hit) {
      statsMBean.updateAttempt(x, y, hit);
      hitRatioMBean.updateStats(hit);
  }

  public static int get_total_attempts(){
    return statsMBean.getTotalAttempts();
  }

  public static int get_total_hits(){
    return statsMBean.getTotalHits();
  }

  public static double get_hit_ratio(){
    return hitRatioMBean.getHitRatio();
  }

  public int getAttemptsCount() {
    return 0;
  }
}
