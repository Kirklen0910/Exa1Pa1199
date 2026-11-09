package com.uth.exa1pa1199.data.remote.dto;

public class CredencialesDto {
    private final String correo;
    private final String password;

    public CredencialesDto(String correo, String password) {
        this.correo = correo;
        this.password = password;
    }

    public String getCorreo() { return correo; }
    public String getPassword() { return password; }
}
