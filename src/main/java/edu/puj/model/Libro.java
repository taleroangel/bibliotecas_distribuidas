package edu.puj.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
public class Libro {
    private String nombre;
    private Long id;
    private Boolean prestado;
    @JsonProperty("fecha_entrega")
    private String fechaEntrega;

    public Date getDate() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(fechaEntrega);
        } catch (ParseException e) {
            return null;
        }
    }

    public void setDate(Date date) {
        fechaEntrega = new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
