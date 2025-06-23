package com.example.frontendquanlikhachsan;

import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.el.parser.Token;

import java.net.URI;
import java.net.http.*;

public class ApiHttpClientCaller {
    private static final String BASE_URL = "http://localhost:8081/api/";
    private static final HttpClient client = HttpClient.newHttpClient();
    public static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    public enum Method {
        GET, POST, PUT, DELETE, PATCH
    }

    public static String call(String path, Method method, Object body, String token) throws Exception {
        return call(path, method, body);
    }

    public static String call(String path, Method method, Object body) throws Exception {
        int currentId= TokenHolder.getInstance().getCurrentUserId();

        if ((method == Method.POST || method == Method.PUT || method == Method.DELETE)
                && !path.contains("authentication")) {
            if (!path.endsWith("/")) path += "/";
            path += currentId + "/" + "Staff";
        }

        String fullUrl = BASE_URL + path;
        String token = TokenHolder.getInstance().getAccessToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl));

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        switch (method) {
            case GET -> requestBuilder.GET();
            case DELETE -> requestBuilder.DELETE();
            case POST -> {
                String json = mapper.writeValueAsString(body);
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json");
            }
            case PUT -> {
                String json = mapper.writeValueAsString(body);
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json");
            }
            case PATCH -> {
                String json = mapper.writeValueAsString(body);
                requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json");
            }
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode == 401) {
            throw new Exception("Unauthorized or other authorization errors: "+response.body());
        } else if (statusCode >= 400) {
            throw new RuntimeException("HTTP error: " + statusCode + ", " + response.body());
        }
        return response.body();
    }
}

