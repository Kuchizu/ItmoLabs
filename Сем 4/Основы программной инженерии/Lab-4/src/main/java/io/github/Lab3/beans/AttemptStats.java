package io.github.Lab3.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import javax.management.*;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Named("attemptStats")
@SessionScoped
public class AttemptStats implements AttemptStatsMBean, Serializable {

  private final AtomicInteger totalAttempts = new AtomicInteger(0);
  private final AtomicInteger totalHits = new AtomicInteger(0);

  private final NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

  @Override
  public int getTotalAttempts() {
    return totalAttempts.get();
  }

  @Override
  public int getTotalHits() {
    return totalHits.get();
  }

  public void updateAttempt(double x, double y, boolean hit) {
    System.out.println("HIT = " + hit);
    totalAttempts.incrementAndGet();
    if (hit) {
      totalHits.incrementAndGet();
    }

    if(Math.abs(x) > 7 || Math.abs(y) > 7){
      System.out.println("MESSAGE");
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Beyond of area", "Coordinates are beyond of the displayed area."));
    }
  }

  public void clearAttempts(){
    totalAttempts.set(0);
    totalHits.set(0);
  }
}
