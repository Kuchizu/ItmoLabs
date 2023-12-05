package io.github.Lab3.model;

import java.io.Serializable;

public enum SelectX implements Serializable {
    MINUS3(-3.0),
    MINUS2(-2.0),
    MINUS1(-1.0),
    PLUS0(0.0),
    PLUS1(1.0),
    PLUS2(2.0),
    PLUS3(3.0),
    UNSELECTED(null);
    private final Double value;
    SelectX(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }
}
