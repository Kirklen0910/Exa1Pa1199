package com.uth.exa1pa1199.data.remote.dto;

public class TokenDto {
    private String token;
    private String expiracion;
    private String rol;
    private String correo;

    public String getToken() { return token; }
    public String getExpiracion() { return expiracion; }
    public String getRol() { return rol; }
    public String getCorreo() { return correo; }
}
