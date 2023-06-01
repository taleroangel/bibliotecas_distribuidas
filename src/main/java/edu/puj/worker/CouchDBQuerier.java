package edu.puj.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.puj.exceptions.ItemNotFoundException;
import edu.puj.model.Libro;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class CouchDBQuerier implements IDatabaseQuerier {
    public static final String DATABASE_NAME = "libros";
    public final String DATABASE_ADDRESS;

    public CouchDBQuerier(String databaseAddress) {
        DATABASE_ADDRESS = databaseAddress;
    }

    @Override
    public Libro getLibro(Long id) throws IOException, ItemNotFoundException {

        Libro found;

        String databaseUrl = String.format("http://%s/%s/%d", DATABASE_ADDRESS, DATABASE_NAME, id);
        System.out.println("COUCHDB/GET\t" + databaseUrl);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            final var httpGet = new HttpGet(databaseUrl);
            final var entity = httpClient.execute(httpGet).getEntity();
            final var mapper = new ObjectMapper();

            final var book = EntityUtils.toString(entity);

            System.out.println("COUCHDB/RES\t" + book);
            found = mapper.readValue(book, Libro.class);
        }

        return found;
    }

    @Override
    public Boolean updateLibro(Libro libro) throws IOException {
        String databaseUrl = String.format("http://%s/%s/%s", DATABASE_ADDRESS, DATABASE_NAME, libro.getId());
        System.out.println("COUCHDB/PUT\t" + databaseUrl);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final var httpPut = new HttpPut(databaseUrl);
            httpPut.setHeader("Content-Type", "application/json");

            final var mapper = new ObjectMapper();
            final var objectAsJson = mapper.writeValueAsString(libro);
            System.out.println("COUCHDB/CONTENT\t" + objectAsJson);

            httpPut.setEntity(new StringEntity(objectAsJson));
            final var entity = httpClient.execute(httpPut);

            // Response is well-formed
            System.out.println("COUCHDB/RES\t" + entity.getStatusLine().getStatusCode());
            return entity.getStatusLine().getStatusCode() == 201;
        }
    }
}
