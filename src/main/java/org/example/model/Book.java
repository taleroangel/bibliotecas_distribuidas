package org.example.model;

import lombok.Value;

import java.util.ArrayList;

@Value
public class Book {
    Integer ID;
    String name;
    String author;
    Integer existence;
    ArrayList<Headquarters> location; //mantener las sedes en donde se tiene el libro
    boolean borrowed;
}
