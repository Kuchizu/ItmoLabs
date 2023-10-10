package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class AreaData implements Serializable {
    private double x;
    private double y;
    private double r;
    private boolean result;
    private long calculationTime;
    private LocalDateTime calculatedAt;

    public AreaData() {
        super();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public boolean getResult() {
        return result;
    }

    public long getCalculationTime() {
        return calculationTime;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setR(double r) {
        this.r = r;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setCalculationTime(long calculationTime) {
        this.calculationTime = calculationTime;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AreaData)) return false;
        AreaData data = (AreaData) o;
        return Double.compare(getX(), data.getX()) == 0 && Double.compare(getY(), data.getY()) == 0 && Double.compare(getR(), data.getR()) == 0 && getResult() == data.getResult() && getCalculationTime() == data.getCalculationTime() && Objects.equals(getCalculatedAt(), data.getCalculatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getR(), getResult(), getCalculationTime(), getCalculatedAt());
    }

    @Override
    public String toString() {
        return "AreaData{" +
                "x=" + x +
                ", y=" + y +
                ", r=" + r +
                ", result=" + result +
                ", calculationTime=" + calculationTime +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}
