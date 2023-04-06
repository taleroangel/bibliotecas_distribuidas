package org.example.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Read operations from files
 */
public class FileReader {

    /**
     * Pattern matching string
     */
    public static final String matchPattern = "([0-9]*;(?:SP|DL|RL))";

    /**
     * Read each line of a file and store it in an ArrayList
     *
     * @param Path Path of the file to be read
     * @return List of lines
     * @throws IOException              File doesn't exist or error while reading file
     * @throws IllegalArgumentException Argument syntax is invalid
     */
    public static ArrayList<String> readLines(String Path) throws IOException, IllegalArgumentException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(Path))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Ejecutar un regex sobre la línea
                assert line.matches(matchPattern);
                lines.add(line);
            }
        } catch (AssertionError ignored) {
            // En caso de que falle el REGEX
            throw new IllegalArgumentException();
        }
        return lines;
    }
}
