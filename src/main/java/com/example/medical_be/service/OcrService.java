package com.example.medical_be.service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.medical_be.dto.res.OcrResponse;
import com.example.medical_be.exception.ApplicationException;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);


    private final WebClient webClient;
    private final int maxRetries;
    private final int timeoutSeconds;

    public OcrService(
            @Value("${ocr.api.url:http://localhost:8000}") String ocrApiUrl,
            @Value("${ocr.api.max-retries:3}") int maxRetries,
            @Value("${ocr.api.timeout-seconds:60}") int timeoutSeconds) {
        this.maxRetries = maxRetries;
        this.timeoutSeconds = timeoutSeconds;
        this.webClient = WebClient.builder()
                .baseUrl(ocrApiUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();
    }

    public OcrResponse processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException("File không được để trống");
        }

        logger.info("Processing OCR for file: {}", file.getOriginalFilename());
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        try {
            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename();
            builder.part("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() { return filename; }
            });
        } catch (Exception e) {
            logger.error("Lỗi đọc file: ", e);
            throw new ApplicationException("Không đọc được file: " + e.getMessage());
        }

        try {
            return webClient.post()
                    .uri("/v1/ocr/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            resp -> resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new ApplicationException("OCR API lỗi: " + body))))
                    .bodyToMono(OcrResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .retryWhen(Retry.max(maxRetries)
                            .filter(throwable -> isRetryable(throwable)))
                    .doOnError(e -> logger.error("OCR processing failed after retries: ", e))
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("OCR API response error - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApplicationException("OCR API lỗi: " + e.getMessage());
        } catch (Exception e) {
            logger.error("OCR processing error: ", e);
            throw new ApplicationException("Lỗi xử lý OCR: " + e.getMessage());
        }
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException e = (WebClientResponseException) throwable;
            return e.getStatusCode().is5xxServerError();
        }
        return throwable instanceof ConnectException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof ReadTimeoutException;
    }
}
