package io.github.Lab3.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;

@Named
@ApplicationScoped
public class TimeBean {

    public LocalDateTime getNowTime() {
        return LocalDateTime.now();
    }

}
