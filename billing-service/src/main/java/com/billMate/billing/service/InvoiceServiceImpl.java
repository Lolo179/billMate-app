package com.billMate.billing.service;

import com.billMate.billing.entity.ClientEntity;
import com.billMate.billing.entity.InvoiceEntity;
import com.billMate.billing.entity.InvoiceLineEntity;
import com.billMate.billing.enums.InvoiceStatus;
import com.billMate.billing.model.InvoiceDTO;
import com.billMate.billing.model.InvoiceLine;

import com.billMate.billing.model.NewInvoiceDTO;
import com.billMate.billing.repository.ClientRepository;
import com.billMate.billing.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoice -> modelMapper.map(invoice, InvoiceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDTO getInvoiceById(Long invoiceId) {
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
        return modelMapper.map(invoice, InvoiceDTO.class);
    }

    @Override
    public InvoiceDTO createInvoice(NewInvoiceDTO newInvoiceDTO) {

        //Validacion del cliente
        ClientEntity client = clientRepository.findById(newInvoiceDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente con ID " + newInvoiceDTO.getClientId() + " no encontrado."));

        // valicacion de líneas
        if (newInvoiceDTO.getInvoiceLines() == null || newInvoiceDTO.getInvoiceLines().isEmpty()) {
            throw new IllegalArgumentException("Una factura debe tener al menos una línea.");
        }
        List<InvoiceLineEntity> validatedLines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (InvoiceLine lineDto : newInvoiceDTO.getInvoiceLines()) {
            if (lineDto.getDescription() == null || lineDto.getDescription().isBlank()) {
                throw new IllegalArgumentException("Cada línea debe tener una descripción.");
            }


            if (lineDto.getUnitPrice() == null || lineDto.getQuantity() == null ||
                    lineDto.getUnitPrice() <= 0 || lineDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Precio y cantidad deben ser mayores a cero.");
            }

            BigDecimal price = BigDecimal.valueOf(lineDto.getUnitPrice());
            BigDecimal quantity = BigDecimal.valueOf(lineDto.getQuantity());

            BigDecimal lineTotal = price.multiply(quantity);
            total = total.add(lineTotal);

            validatedLines.add(
                    InvoiceLineEntity.builder()
                            .description(lineDto.getDescription())
                            .quantity(quantity)
                            .unitPrice(price)
                            .total(lineTotal)
                            .build()
            );
        }

        //Calcular IVA (21%)
        BigDecimal taxPercentage = BigDecimal.valueOf(21);
        BigDecimal taxAmount = total.multiply(taxPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal finalTotal = total.add(taxAmount);


        //Construir la entidad y persistir
        InvoiceEntity entity = InvoiceEntity.builder()
                .client(client)
                .date(newInvoiceDTO.getDate())
                .description(newInvoiceDTO.getDescription())
                .status(newInvoiceDTO.getStatus() != null
                        ? InvoiceStatus.valueOf(newInvoiceDTO.getStatus().name())
                        : InvoiceStatus.DRAFT)
                .invoiceLines(validatedLines)
                .taxPercentage(taxPercentage)
                .total(finalTotal)
                .createdAt(LocalDateTime.now())
                .build();

        // Asignar la relación inversa (line -> invoice)
        validatedLines.forEach(line -> line.setInvoice(entity));

        InvoiceEntity saved = invoiceRepository.save(entity);
        return modelMapper.map(saved, InvoiceDTO.class);

    }

    @Override
    public InvoiceDTO updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        ClientEntity client = clientRepository.findById(newInvoiceDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        invoice.setClient(client);
        invoice.setDate(newInvoiceDTO.getDate());
        invoice.setDescription(newInvoiceDTO.getDescription());
        invoice.setStatus(InvoiceStatus.valueOf(newInvoiceDTO.getStatus().name()));

        invoice.getInvoiceLines().clear();

        BigDecimal total = BigDecimal.ZERO;

        for (InvoiceLine lineDto : newInvoiceDTO.getInvoiceLines()) {
            if (lineDto.getDescription() == null || lineDto.getDescription().isBlank()) {
                throw new IllegalArgumentException("Cada línea debe tener una descripción.");
            }

            if (lineDto.getUnitPrice() == null || lineDto.getQuantity() == null ||
                    lineDto.getUnitPrice() <= 0 || lineDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Precio y cantidad deben ser mayores a cero.");
            }

            BigDecimal price = BigDecimal.valueOf(lineDto.getUnitPrice());
            BigDecimal quantity = BigDecimal.valueOf(lineDto.getQuantity());
            BigDecimal lineTotal = price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

            InvoiceLineEntity line = InvoiceLineEntity.builder()
                    .description(lineDto.getDescription())
                    .quantity(quantity)
                    .unitPrice(price)
                    .total(lineTotal)
                    .invoice(invoice)
                    .build();

            invoice.getInvoiceLines().add(line);
            total = total.add(lineTotal);
        }

        invoice.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        InvoiceEntity saved = invoiceRepository.save(invoice);
        return modelMapper.map(saved, InvoiceDTO.class);
    }

    @Override
    public void deleteInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new EntityNotFoundException("Factura no encontrada");
        }
        invoiceRepository.deleteById(invoiceId);
    }

    @Override
    public List<InvoiceDTO> getInvoicesByClientId(Long clientId) {
        return invoiceRepository.findAllByClient_Id(clientId).stream()
                .map(invoice -> modelMapper.map(invoice, InvoiceDTO.class))
                .collect(Collectors.toList());
    }
}

