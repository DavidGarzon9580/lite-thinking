package com.litethinking.platform.orders.service;

import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.catalog.repository.ProductoRepository;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import com.litethinking.platform.orders.domain.Cliente;
import com.litethinking.platform.orders.domain.Orden;
import com.litethinking.platform.orders.domain.OrdenItem;
import com.litethinking.platform.orders.dto.ClienteResponse;
import com.litethinking.platform.orders.dto.OrdenItemRequest;
import com.litethinking.platform.orders.dto.OrdenRequest;
import com.litethinking.platform.orders.dto.OrdenResponse;
import com.litethinking.platform.orders.repository.ClienteRepository;
import com.litethinking.platform.orders.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;

    public OrdenService(OrdenRepository ordenRepository,
                        ClienteRepository clienteRepository,
                        EmpresaRepository empresaRepository,
                        ProductoRepository productoRepository) {
        this.ordenRepository = ordenRepository;
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public OrdenResponse crear(OrdenRequest request) {
        Empresa empresa = empresaRepository.findById(request.empresaNit())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Cliente cliente = clienteRepository.findByCorreo(request.clienteCorreo())
                .map(existing -> {
                    existing.update(request.clienteNombre());
                    return existing;
                })
                .orElseGet(() -> clienteRepository.save(new Cliente(request.clienteNombre(), request.clienteCorreo())));

        Orden orden = new Orden(cliente, empresa);
        Set<OrdenItem> items = mapItems(request.items(), empresa.getNit());
        orden.replaceItems(items);

        Orden saved = ordenRepository.save(orden);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorEmpresa(String empresaNit) {
        return ordenRepository.findByEmpresaNit(empresaNit).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(UUID id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        return toResponse(orden);
    }

    private Set<OrdenItem> mapItems(List<OrdenItemRequest> itemsRequest, String empresaNit) {
        Set<OrdenItem> items = new HashSet<>();
        for (OrdenItemRequest itemRequest : itemsRequest) {
            Producto producto = productoRepository.findById(itemRequest.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            if (!producto.getEmpresa().getNit().equals(empresaNit)) {
                throw new IllegalArgumentException("El producto no pertenece a la empresa seleccionada");
            }
            OrdenItem item = new OrdenItem(producto, itemRequest.cantidad(), itemRequest.precioUnitario());
            items.add(item);
        }
        return items;
    }

    private OrdenResponse toResponse(Orden orden) {
        List<OrdenResponse.Item> items = orden.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
                    return new OrdenResponse.Item(
                            item.getProducto().getId(),
                            item.getProducto().getNombre(),
                            item.getCantidad(),
                            item.getPrecioUnitario(),
                            subtotal
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(OrdenResponse.Item::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Cliente cliente = orden.getCliente();
        ClienteResponse clienteResponse = new ClienteResponse(cliente.getId(), cliente.getNombre(), cliente.getCorreo());

        return new OrdenResponse(
                orden.getId(),
                orden.getFecha(),
                orden.getEmpresa().getNit(),
                clienteResponse,
                items,
                total
        );
    }
}
