package se.s373746.Lab4.payload.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest implements Serializable {

    private String username;
    private String password;

}
