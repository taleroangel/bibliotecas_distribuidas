package edu.puj.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.puj.exceptions.ItemNotFoundException;
import edu.puj.model.Libro;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class CouchDBQuerierI implements IDatabaseQuerier {

    public static final String QUERY_FORMAT = "{\"selector\":{\"id\":%d},\"fields\":[\"id\",\"nombre\",\"prestado\",\"fecha_entrega\"]}";

    public static final String DATABASE_NAME = "libros";
    public final String DATABASE_ADDRESS;

    public CouchDBQuerierI(String databaseAddress) {
        DATABASE_ADDRESS = databaseAddress;
    }

    @Override
    public Libro getLibro(Long id) throws IOException, ItemNotFoundException {

        Libro found;

        String query = String.format(QUERY_FORMAT, id);
        String databaseUrl = String.format("http://%s/%s/_find", DATABASE_ADDRESS, DATABASE_NAME);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final var httpPost = new HttpPost(databaseUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(query));

            final var entity = httpClient.execute(httpPost).getEntity();


            final var mapper = new ObjectMapper();
            final var documents = mapper.readTree(entity.getContent()).get("docs");

            if (!documents.has(0)) {
                throw new ItemNotFoundException();
            }

            final var serializedObject = documents.get(0);

            found = mapper.readValue(serializedObject.toString(), Libro.class);
        }

        return found;
    }

    @Override
    public Boolean updateLibro(Libro libro) {
        return true;
    }
}
