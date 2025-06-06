package com.example.frontendquanlikhachsan;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class ApiHttpClientCaller {
    private static final String BASE_URL = "http://localhost:8080/api/";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public static String call(String path, Method method, Object body, String token) throws Exception {
        String fullUrl = BASE_URL + path;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + token);

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
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}

