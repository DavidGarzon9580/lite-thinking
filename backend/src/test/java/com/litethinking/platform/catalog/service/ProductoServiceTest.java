package com.litethinking.platform.catalog.service;

import com.litethinking.platform.catalog.domain.Categoria;
import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.domain.ProductoPrecio;
import com.litethinking.platform.catalog.dto.PrecioRequest;
import com.litethinking.platform.catalog.dto.ProductoRequest;
import com.litethinking.platform.catalog.mapper.ProductoMapper;
import com.litethinking.platform.catalog.repository.CategoriaRepository;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.catalog.repository.ProductoRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    private final ProductoMapper productoMapper = new ProductoMapper() { };
    private ProductoService productoService;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, empresaRepository, categoriaRepository, productoMapper);
        empresa = new Empresa("123", "Lite", "Dir", "Tel");
    }

    @Test
    void crearDebePersistirProductoYNuevasCategorias() {
        ProductoRequest request = new ProductoRequest(
                "PROD-01",
                "Laptop",
                "16 GB RAM",
                empresa.getNit(),
                List.of(new PrecioRequest("USD", BigDecimal.valueOf(1500))),
                List.of("Tecnologia", "Computadores")
        );

        when(empresaRepository.findById(empresa.getNit())).thenReturn(Optional.of(empresa));
        when(productoRepository.existsByCodigoAndEmpresaNit("PROD-01", empresa.getNit())).thenReturn(false);
        when(categoriaRepository.findByNombre(any())).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productoService.crear(request);

        assertThat(response.codigo()).isEqualTo("PROD-01");
        assertThat(response.categorias()).containsExactlyInAnyOrder("Tecnologia", "Computadores");
        assertThat(response.precios()).hasSize(1);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearDebeFallarCuandoEmpresaNoExiste() {
        ProductoRequest request = new ProductoRequest(
                "PROD-01",
                "Laptop",
                "16 GB RAM",
                "404",
                List.of(new PrecioRequest("USD", BigDecimal.ONE)),
                List.of()
        );

        when(empresaRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearDebeFallarCuandoProductoDuplicado() {
        ProductoRequest request = new ProductoRequest(
                "PROD-01",
                "Laptop",
                "16 GB RAM",
                empresa.getNit(),
                List.of(new PrecioRequest("USD", BigDecimal.ONE)),
                List.of()
        );

        when(empresaRepository.findById(empresa.getNit())).thenReturn(Optional.of(empresa));
        when(productoRepository.existsByCodigoAndEmpresaNit("PROD-01", empresa.getNit())).thenReturn(true);

        assertThatThrownBy(() -> productoService.crear(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizarDebeModificarNombreCaracteristicasYPrecios() {
        UUID id = UUID.randomUUID();
        Producto producto = new Producto("PROD-01", "Laptop", "Antigua", empresa);
        producto.replacePrecios(Set.of(new ProductoPrecio("USD", BigDecimal.valueOf(1000))));
        producto.setCategorias(Set.of(new Categoria("Tecnologia")));

        ProductoRequest request = new ProductoRequest(
                "PROD-01",
                "Laptop Actualizada",
                "Pantalla 4K",
                empresa.getNit(),
                List.of(new PrecioRequest("USD", BigDecimal.valueOf(1800))),
                List.of("Tecnologia")
        );

        when(productoRepository.findById(id)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findByNombre("Tecnologia")).thenReturn(Optional.of(new Categoria("Tecnologia")));

        var response = productoService.actualizar(id, request);

        assertThat(response.nombre()).isEqualTo("Laptop Actualizada");
        assertThat(response.caracteristicas()).isEqualTo("Pantalla 4K");
        assertThat(response.precios()).extracting("valor").containsExactly(BigDecimal.valueOf(1800));
    }

    @Test
    void eliminarDebeFallarCuandoNoExisteProducto() {
        UUID id = UUID.randomUUID();
        when(productoRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> productoService.eliminar(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
