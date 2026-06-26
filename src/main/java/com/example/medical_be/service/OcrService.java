package com.example.medical_be.service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

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
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.medical_be.dto.res.OcrResponse;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);

    private final WebClient webClient;
    private final int maxRetries;
    private final int timeoutSeconds;
    private final IMessageTranslator messageTranslator;

    public OcrService(
            @Value("${ocr.api.url:http://localhost:8000}") String ocrApiUrl,
            @Value("${ocr.api.max-retries:3}") int maxRetries,
            @Value("${ocr.api.timeout-seconds:60}") int timeoutSeconds,
            IMessageTranslator messageTranslator) {
        this.maxRetries = maxRetries;
        this.timeoutSeconds = timeoutSeconds;
        this.messageTranslator = messageTranslator;
        this.webClient = WebClient.builder()
                .baseUrl(ocrApiUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();
    }

    public OcrResponse processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException(messageTranslator.getMessage("ocr.file.empty"));
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
            logger.error("Failed to read file bytes: ", e);
            throw new ApplicationException(messageTranslator.getMessage("ocr.file.read_failed"));
        }

        try {
            return webClient.post()
                    .uri("/v1/ocr/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            resp -> resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new ApplicationException(
                                                    messageTranslator.getMessage("ocr.api.error")))))
                    .bodyToMono(OcrResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .retryWhen(Retry.max(maxRetries)
                            .filter(this::isRetryable))
                    .doOnError(e -> logger.error("OCR processing failed after retries: ", e))
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("OCR API response error - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApplicationException(messageTranslator.getMessage("ocr.api.error"));
        } catch (Exception e) {
            logger.error("OCR processing error: ", e);
            throw new ApplicationException(messageTranslator.getMessage("ocr.processing.failed"));
        }
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException e) {
                return e.getStatusCode().is5xxServerError();
            }
            if (current instanceof WebClientRequestException
                    || current instanceof ConnectException
                    || current instanceof SocketTimeoutException
                    || current instanceof ReadTimeoutException
                    || current instanceof TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
