package com.litethinking.platform.catalog.service;

import com.litethinking.platform.catalog.domain.Categoria;
import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.domain.ProductoPrecio;
import com.litethinking.platform.catalog.dto.ProductoRequest;
import com.litethinking.platform.catalog.dto.ProductoResponse;
import com.litethinking.platform.catalog.mapper.ProductoMapper;
import com.litethinking.platform.catalog.repository.CategoriaRepository;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.catalog.repository.ProductoRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(ProductoRepository productoRepository,
                           EmpresaRepository empresaRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Empresa empresa = empresaRepository.findById(request.empresaNit())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        if (productoRepository.existsByCodigoAndEmpresaNit(request.codigo(), request.empresaNit())) {
            throw new ResourceAlreadyExistsException("El producto ya existe para la empresa");
        }

        Producto producto = new Producto(
                request.codigo(),
                request.nombre(),
                request.caracteristicas(),
                empresa
        );

        Set<ProductoPrecio> precios = productoMapper.toPrecioEntities(request.precios());
        precios.forEach(precio -> precio.setProducto(producto));
        producto.replacePrecios(precios);
        producto.setCategorias(resolveCategorias(request.categorias()));

        Producto saved = productoRepository.save(producto);
        return productoMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorEmpresa(String empresaNit) {
        return productoRepository.findByEmpresaNit(empresaNit).stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(UUID id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return productoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        producto.update(request.nombre(), request.caracteristicas());
        Set<ProductoPrecio> precios = productoMapper.toPrecioEntities(request.precios());
        precios.forEach(precio -> precio.setProducto(producto));
        producto.replacePrecios(precios);
        producto.setCategorias(resolveCategorias(request.categorias()));

        return productoMapper.toResponse(producto);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    private Set<Categoria> resolveCategorias(List<String> nombres) {
        Set<Categoria> categorias = new HashSet<>();
        for (String nombre : nombres) {
            Categoria categoria = categoriaRepository.findByNombre(nombre)
                    .orElseGet(() -> categoriaRepository.save(new Categoria(nombre)));
            categorias.add(categoria);
        }
        return categorias;
    }
}
