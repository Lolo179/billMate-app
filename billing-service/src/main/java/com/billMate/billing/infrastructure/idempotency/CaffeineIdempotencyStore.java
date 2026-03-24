package com.billMate.billing.infrastructure.idempotency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementación in-memory de {@link IdempotencyStore} usando Caffeine.
 * <p>
 * Política de caché:
 * <ul>
 *   <li>TTL: 24 horas tras la escritura</li>
 *   <li>Tamaño máximo: 10.000 entradas (evita consumo excesivo de memoria)</li>
 * </ul>
 */
@Component
public class CaffeineIdempotencyStore implements IdempotencyStore {

    private final Cache<String, IdempotencyRecord> cache;

    public CaffeineIdempotencyStore() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10_000)
                .build();
    }

    @Override
    public Optional<IdempotencyRecord> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void save(String key, IdempotencyRecord record) {
        cache.put(key, record);
    }
}
