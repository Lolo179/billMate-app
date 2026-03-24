package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.PatchClientUseCase;
import com.billMate.billing.domain.client.port.in.command.PatchClientCommand;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Servicio de actualización parcial de cliente (JSON Merge Patch, RFC 7396).
 * Solo aplica los campos presentes en el comando (Optional.isPresent()).
 * Si un campo está ausente (Optional.empty()), se mantiene el valor existente.
 *
 * Nota: no se soporta el borrado de campos a null (enviando null en el JSON).
 * Para ese caso, usar PUT con el recurso completo.
 */
@Service
public class PatchClientService implements PatchClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(PatchClientService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public PatchClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(PatchClientCommand command) {
        log.info("Patching client", kv("clientId", command.clientId()));

        Client client = clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> {
                    log.warn("Client not found for patch", kv("clientId", command.clientId()));
                    return new EntityNotFoundException("Cliente con ID " + command.clientId() + " no encontrado.");
                });

        // Aplicar solo los campos presentes en el payload
        command.name().ifPresent(client::setName);
        command.email().ifPresent(client::setEmail);
        command.phone().ifPresent(client::setPhone);
        command.nif().ifPresent(client::setNif);
        command.address().ifPresent(client::setAddress);

        Client updated = clientRepositoryPort.save(client);
        log.info("Client patched", kv("clientId", updated.getId()));
        return updated;
    }
}
