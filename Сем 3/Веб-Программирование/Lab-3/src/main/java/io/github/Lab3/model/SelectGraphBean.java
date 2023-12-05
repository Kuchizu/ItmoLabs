package io.github.Lab3.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Objects;

@Named
@ApplicationScoped
public class SelectGraphBean implements Serializable {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectGraphBean)) return false;
        SelectGraphBean that = (SelectGraphBean) o;
        return Double.compare(getX(), that.getX()) == 0 && Double.compare(getY(), that.getY()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "SelectGraphBean{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
