package com.billMate.billing.infrastructure.idempotency;

import com.billMate.billing.infrastructure.rest.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Filtro de idempotencia para peticiones POST.
 * <p>
 * Flujo:
 * <ol>
 *   <li>Si el método no es POST o no hay cabecera {@code Idempotency-Key}, deja pasar la petición.</li>
 *   <li>Valida que el valor sea un UUID bien formado (error 400 si no).</li>
 *   <li>Si la clave ya existe en la caché, devuelve la respuesta almacenada sin reejecutar el handler.</li>
 *   <li>Si la clave es nueva, envuelve la respuesta, ejecuta el handler y — si el estado es 2xx — guarda la respuesta en caché.</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String HEADER = "Idempotency-Key";

    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Solo aplica a POST
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String keyHeader = request.getHeader(HEADER);

        // Sin cabecera → petición normal sin idempotencia
        if (keyHeader == null || keyHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validar UUID
        if (!isValidUUID(keyHeader)) {
            log.warn("Idempotency-Key inválida, no es un UUID", kv("key", keyHeader));
            writeError(response, HttpStatus.BAD_REQUEST, "La cabecera Idempotency-Key debe ser un UUID válido.");
            return;
        }

        String key = keyHeader.trim();

        // Comprobar caché
        Optional<IdempotencyRecord> cached = idempotencyStore.get(key);
        if (cached.isPresent()) {
            log.debug("Idempotency cache hit, replaying response", kv("key", key));
            replayResponse(response, cached.get());
            return;
        }

        // Ejecutar handler y capturar respuesta
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        int status = wrapper.getStatus();
        byte[] body = wrapper.getContentAsByteArray();
        String contentType = wrapper.getContentType();

        // Guardar solo respuestas 2xx
        if (status >= 200 && status < 300) {
            idempotencyStore.save(key, new IdempotencyRecord(status, body, contentType));
            log.debug("Idempotency response cached", kv("key", key), kv("status", status));
        }

        // Siempre escribir la respuesta real al cliente
        wrapper.copyBodyToResponse();
    }

    private void replayResponse(HttpServletResponse response, IdempotencyRecord record)
            throws IOException {
        response.setStatus(record.status());
        if (record.contentType() != null) {
            response.setContentType(record.contentType());
        }
        response.getOutputStream().write(record.body());
    }

    private boolean isValidUUID(String value) {
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        ApiError error = new ApiError()
                .status(status.name())
                .code(status.value())
                .message(message)
                .timestamp(OffsetDateTime.now());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
