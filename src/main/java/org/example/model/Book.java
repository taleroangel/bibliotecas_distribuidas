package org.example.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Book {
    private Integer ID;
    private String name;
    private String author;
    private Integer existence;
    private ArrayList<Headquarters> location; //mantener las sedes en donde se tiene el libro
    private boolean borrowed;


}
