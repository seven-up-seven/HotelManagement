package com.example.frontendquanlikhachsan;

import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;

public class ApiHttpClientCaller {
    private static final String BASE_URL = "http://localhost:8080/api/";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public static String call(String path, Method method, Object body) throws Exception {
        String fullUrl = BASE_URL + path;
        String token=TokenHolder.getInstance().getAccessToken();

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

//    public static boolean refreshAccessToken() {
//        try {
//            String refreshToken = TokenHolder.getInstance().getRefreshToken();
//            RefreshDto refreshDto = new RefreshDto(refreshToken);
//
//            String jsonResponse = call(
//                    "authentication/refresh",
//                    Method.POST,
//                    refreshDto,
//                    null
//            );
//            ResponseRefreshDto response = mapper.readValue(jsonResponse, ResponseRefreshDto.class);
//            TokenHolder.getInstance().setAccessToken(response.accessToken());
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static String callWithAutoRefresh(String path, Method method, Object body) throws Exception {
//        String token = TokenHolder.getInstance().getAccessToken();
//        try {
//            return call(path, method, body, token);
//        } catch (UnauthorizedException e) {
//            boolean refreshed = refreshAccessToken();
//            if (refreshed) {
//                token = TokenHolder.getInstance().getAccessToken();
//                return call(path, method, body, token);
//            } else {
//                throw new RuntimeException("Session expired. Please login again.");
//            }
//        }
//    }
}

