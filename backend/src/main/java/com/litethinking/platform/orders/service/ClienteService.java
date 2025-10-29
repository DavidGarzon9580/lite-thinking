package com.litethinking.platform.orders.service;

import com.litethinking.platform.orders.domain.Cliente;
import com.litethinking.platform.orders.dto.ClienteRequest;
import com.litethinking.platform.orders.dto.ClienteResponse;
import com.litethinking.platform.orders.repository.ClienteRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        clienteRepository.findByCorreo(request.correo()).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException("El cliente ya existe");
        });

        Cliente cliente = new Cliente(request.nombre(), request.correo());
        Cliente saved = clienteRepository.save(cliente);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClienteResponse actualizar(UUID id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        cliente.update(request.nombre());
        return toResponse(cliente);
    }

    public ClienteResponse obtener(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return toResponse(cliente);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNombre(), cliente.getCorreo());
    }
}
