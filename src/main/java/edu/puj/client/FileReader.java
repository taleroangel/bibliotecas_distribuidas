package edu.puj.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Read operations from files
 */
public class FileReader {

    /**
     * Maximum number of operations in file
     */
    public static final int MAXIMUM_LENGTH = 20;

    /**
     * Pattern matching string
     */
    public static final String MATCH_PATTERN = "([0-9]*;[SDR])";

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
                assert line.matches(MATCH_PATTERN);
                lines.add(line);
            }
            assert lines.size() <= MAXIMUM_LENGTH;
        } catch (AssertionError ignored) {
            // En caso de que falle el REGEX
            throw new IllegalArgumentException();
        }
        return lines;
    }
}
