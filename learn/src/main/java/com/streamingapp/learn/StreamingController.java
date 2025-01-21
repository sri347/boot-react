package com.streamingapp.learn;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class StreamingController {

    @Autowired
    private StreamingService streamingService;

    @GetMapping(value = "/stream-json", produces = MediaType.APPLICATION_JSON_VALUE)
    public StreamingResponseBody streamJson() {
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try {
                    for (int i = 1; i <= 5; i++) {
                        String jsonChunk = "{\"data\":\"Chunk " + i + "\"}\n";
                        outputStream.write(jsonChunk.getBytes());
                        outputStream.flush();
                        TimeUnit.SECONDS.sleep(1); // Simulate a delay for each chunk
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    @GetMapping("/consume-stream")
    public StreamingResponseBody consumeStream() {
        return streamingService.consumeStream();
    }

    @GetMapping("/consume-streamv2")
    public String consumeAndProcessStream() {
        streamingService.consumeAndProcessStream();
        // Return a message or processed data
        return "Streaming request completed. Check logs for real-time chunks.";
    }
}
