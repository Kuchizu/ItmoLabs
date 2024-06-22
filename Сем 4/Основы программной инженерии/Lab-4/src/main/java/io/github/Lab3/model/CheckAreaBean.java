package io.github.Lab3.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Named
@Entity
@Table(name = "results", schema = "s373746")
@ApplicationScoped
public class CheckAreaBean implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "x")
    private double x;
    @Column(name = "y")
    private double y;
    @Column(name = "r")
    private double r;
    @Column(name = "result")
    private boolean result;
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    @Column(name = "exec_time")
    private long execTime;

    public CheckAreaBean() {
        super();
    }

    @Column(name = "x")
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Column(name = "y")
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Column(name = "r")
    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    @Column(name = "result")
    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Column(name = "executed_at")
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    @Column(name = "exec_time")
    public long getExecTime() {
        return execTime;
    }

    public void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckAreaBean)) return false;
        CheckAreaBean bean = (CheckAreaBean) o;
        return getId() == bean.getId() && Double.compare(getX(), bean.getX()) == 0 && Double.compare(getY(), bean.getY()) == 0 && Double.compare(getR(), bean.getR()) == 0 && isResult() == bean.isResult() && getExecTime() == bean.getExecTime() && Objects.equals(getExecutedAt(), bean.getExecutedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getX(), getY(), getR(), isResult(), getExecutedAt(), getExecTime());
    }

    @Override
    public String toString() {
        return "CheckAreaBean{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", r=" + r +
                ", result=" + result +
                ", executedAt=" + executedAt +
                ", execTime=" + execTime +
                '}';
    }
}
