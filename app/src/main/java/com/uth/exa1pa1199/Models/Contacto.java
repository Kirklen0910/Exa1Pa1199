package com.uth.exa1pa1199.Models;

public class Contacto {
    private int id;
    private String pais;
    private String codigoArea;
    private String nombre;
    private String telefono;
    private String nota;
    private String imagenUri;

    public Contacto() {}

    public Contacto(int id, String pais, String codigoArea, String nombre, String telefono, String nota, String imagenUri) {
        this.id = id;
        this.pais = pais;
        this.codigoArea = codigoArea;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagenUri = imagenUri;
    }

    public Contacto(String pais, String codigoArea, String nombre, String telefono, String nota, String imagenUri) {
        this(0, pais, codigoArea, nombre, telefono, nota, imagenUri);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCodigoArea() { return codigoArea; }
    public void setCodigoArea(String codigoArea) { this.codigoArea = codigoArea; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public String getImagenUri() { return imagenUri; }
    public void setImagenUri(String imagenUri) { this.imagenUri = imagenUri; }
}
