package se.s373746.Lab4.payload.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenRefreshRequest implements Serializable {

    private String refreshToken;

}
