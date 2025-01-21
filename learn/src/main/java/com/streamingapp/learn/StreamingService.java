package com.streamingapp.learn;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
public class StreamingService {

    @Autowired
    private RestTemplate restTemplate;

    public StreamingResponseBody consumeStream() {
        System.out.println("consumeStream");
        return outputStream -> {
            RequestCallback requestCallback = request -> {
                request.getHeaders().set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                System.out.println("Request initiated to consume streaming response.");
            };

            ResponseExtractor<Void> responseExtractor = response -> {
                InputStream inputStream = response.getBody();
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Read from the input stream and write to the output stream in real-time
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    System.out.println("Received a chunk of size: " + bytesRead);
                    String chunk = new String(buffer, 0, bytesRead);
                    System.out.println("Chunk content: " + chunk);

                    // Write and flush the chunk to the output stream
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush(); // Ensure the data is flushed immediately
                }
                System.out.println("Streaming completed.");
                return null;
            };

            // Make the API call and stream the response
            restTemplate.execute(URI.create("http://localhost:8090/stream-json"), HttpMethod.GET, requestCallback,
                    responseExtractor);
        };
    }

    public void consumeAndProcessStream() {
        System.out.println("consumeAndProcessStream");
        RequestCallback requestCallback = request -> {
            request.getHeaders().set("Accept", "application/json");
            System.out.println("Request initiated to consume streaming response.");
        };

        ResponseExtractor<Void> responseExtractor = response -> {
            InputStream inputStream = response.getBody();
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the stream chunk by chunk
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                System.out.println("Received a chunk of size: " + bytesRead);
                System.out.println("Chunk content: " + chunk);

                // Simulate some processing on the chunk here, if needed
            }
            System.out.println("Streaming completed.");
            return null;
        };

        // Make the API call and stream the response
        restTemplate.execute("http://localhost:8090/stream-json", HttpMethod.GET, requestCallback, responseExtractor);
    }
}
