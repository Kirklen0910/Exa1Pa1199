package com.uth.exa1pa1199.data.remote.dto;

import java.util.List;
import java.util.Map;

/**
 * Cuerpo de error de ASP.NET Core (ProblemDetails / ValidationProblemDetails).
 * En un 400, "errors" trae los mensajes por campo, p. ej. {"Titulo": ["..."]}.
 */
public class ProblemDetailsDto {
    private String title;
    private String detail;
    private Integer status;
    private Map<String, List<String>> errors;

    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public Integer getStatus() { return status; }
    public Map<String, List<String>> getErrors() { return errors; }
}
