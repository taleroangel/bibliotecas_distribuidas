package edu.puj.worker;

import edu.puj.exceptions.ItemNotFoundException;
import edu.puj.model.Libro;

import java.io.IOException;

public interface IDatabaseQuerier {
    Libro getLibro(Long id) throws IOException, ItemNotFoundException;

    Boolean updateLibro(Libro libro) throws IOException;
}
