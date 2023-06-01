package edu.puj.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @JsonProperty("_id")
    private String id;
    private Boolean prestado;
    @JsonProperty("fecha_entrega")
    private String fechaEntrega;

    @JsonProperty("_rev")
    private String rev;


    @JsonIgnore
    public Date getDate() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(fechaEntrega);
        } catch (ParseException e) {
            return null;
        }
    }

    @JsonIgnore
    public void setDate(Date date) {
        fechaEntrega = new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
