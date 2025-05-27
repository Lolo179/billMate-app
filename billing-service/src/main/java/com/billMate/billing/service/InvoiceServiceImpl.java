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

import java.time.LocalDateTime;
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
        ClientEntity client = clientRepository.findById(newInvoiceDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        InvoiceEntity invoice = modelMapper.map(newInvoiceDTO, InvoiceEntity.class);
        invoice.setClient(client);

        for (InvoiceLineEntity line : invoice.getInvoiceLines()) {
            line.setInvoice(invoice);
            line.calcularTotal();
        }

        invoice.recalculateTotal();
        invoice.setCreatedAt(LocalDateTime.now());

        InvoiceEntity saved = invoiceRepository.save(invoice);

        return modelMapper.map(saved, InvoiceDTO.class);
    }

    @Override
    public InvoiceDTO updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        invoice.setDate(newInvoiceDTO.getDate());
        invoice.setDescription(newInvoiceDTO.getDescription());
        invoice.setStatus(InvoiceStatus.valueOf(newInvoiceDTO.getStatus().name()));

        invoice.getInvoiceLines().clear();
        for (InvoiceLine lineDto : newInvoiceDTO.getInvoiceLines()) {
            InvoiceLineEntity line = modelMapper.map(lineDto, InvoiceLineEntity.class);
            line.setInvoice(invoice);
            line.calcularTotal();
            invoice.getInvoiceLines().add(line);
        }

        invoice.recalculateTotal();
        InvoiceEntity updated = invoiceRepository.save(invoice);
        return modelMapper.map(updated, InvoiceDTO.class);
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

