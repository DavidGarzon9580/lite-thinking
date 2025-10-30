package com.litethinking.platform.common.config;

import com.litethinking.platform.auth.domain.UserAccount;
import com.litethinking.platform.auth.domain.UserRole;
import com.litethinking.platform.auth.repository.UserAccountRepository;
import com.litethinking.platform.catalog.domain.Categoria;
import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.domain.ProductoPrecio;
import com.litethinking.platform.catalog.repository.CategoriaRepository;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.catalog.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadSampleData(UserAccountRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     EmpresaRepository empresaRepository,
                                     ProductoRepository productoRepository,
                                     CategoriaRepository categoriaRepository) {
        return args -> {
            if (!userRepository.existsByEmail("admin@litethinking.com")) {
                UserAccount admin = new UserAccount(
                        "admin@litethinking.com",
                        passwordEncoder.encode("Admin123*"),
                        UserRole.ADMIN
                );
                userRepository.save(admin);
            }

            if (!userRepository.existsByEmail("viewer@litethinking.com")) {
                UserAccount viewer = new UserAccount(
                        "viewer@litethinking.com",
                        passwordEncoder.encode("Viewer123*"),
                        UserRole.VIEWER
                );
                userRepository.save(viewer);
            }

            Categoria tecnologia = ensureCategoria(categoriaRepository, "Tecnologia");
            Categoria serviciosCloud = ensureCategoria(categoriaRepository, "Servicios Cloud");
            Categoria softwareEmpresarial = ensureCategoria(categoriaRepository, "Software Empresarial");
            Categoria hardware = ensureCategoria(categoriaRepository, "Hardware Profesional");
            Categoria analytics = ensureCategoria(categoriaRepository, "Analitica de Datos");

            Empresa liteThinking = ensureEmpresa(empresaRepository, "900123456", "Lite Thinking", "Calle 1 #2-3", "+57 312 000 1111");
            ensureProducto(
                    productoRepository,
                    liteThinking,
                    "PROD-001",
                    "Laptop Pro",
                    "Laptop de 16GB RAM pensada para desarrollo y diseno.",
                    Map.of("COP", new BigDecimal("4500000"), "USD", new BigDecimal("1100")),
                    Set.of(tecnologia, hardware)
            );

            Empresa novaCloud = ensureEmpresa(empresaRepository, "901234567", "Nova Cloud Services", "Carrera 7 # 45-90", "+57 601 456 7890");
            ensureProducto(
                    productoRepository,
                    novaCloud,
                    "PROD-200",
                    "Plataforma CRM SaaS",
                    "Suite CRM multi-tenant con integraciones nativas a canales digitales.",
                    Map.of("COP", new BigDecimal("1800000"), "USD", new BigDecimal("420")),
                    Set.of(serviciosCloud, softwareEmpresarial)
            );

            Empresa dataVision = ensureEmpresa(empresaRepository, "902345678", "DataVision Analytics", "Av. 3N # 56-21", "+57 2 389 7766");
            ensureProducto(
                    productoRepository,
                    dataVision,
                    "PROD-310",
                    "Motor de Analitica Predictiva",
                    "Motor de IA para pronosticos de demanda y dashboards en tiempo real.",
                    Map.of("COP", new BigDecimal("5200000"), "USD", new BigDecimal("1350")),
                    Set.of(analytics, tecnologia)
            );

            Empresa infraPlus = ensureEmpresa(empresaRepository, "903456789", "InfraPlus Networks", "Transversal 20 # 100-15", "+57 601 320 4455");
            ensureProducto(
                    productoRepository,
                    infraPlus,
                    "PROD-415",
                    "Firewall Gestionado + SD-WAN",
                    "Dispositivo de seguridad perimetral con monitoreo 24/7 y gestion SD-WAN.",
                    Map.of("COP", new BigDecimal("6800000"), "USD", new BigDecimal("1690")),
                    Set.of(hardware, serviciosCloud)
            );
        };
    }

    private Categoria ensureCategoria(CategoriaRepository categoriaRepository, String nombre) {
        return categoriaRepository.findByNombre(nombre)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nombre)));
    }

    private Empresa ensureEmpresa(EmpresaRepository empresaRepository, String nit, String nombre, String direccion, String telefono) {
        return empresaRepository.findById(nit)
                .orElseGet(() -> empresaRepository.save(new Empresa(nit, nombre, direccion, telefono)));
    }

    private void ensureProducto(ProductoRepository productoRepository,
                                Empresa empresa,
                                String codigo,
                                String nombre,
                                String caracteristicas,
                                Map<String, BigDecimal> precios,
                                Set<Categoria> categorias) {
        if (productoRepository.existsByCodigoAndEmpresaNit(codigo, empresa.getNit())) {
            return;
        }
        Producto producto = new Producto(codigo, nombre, caracteristicas, empresa);
        Set<ProductoPrecio> preciosEntidad = precios.entrySet().stream()
                .map(entry -> new ProductoPrecio(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        producto.replacePrecios(preciosEntidad);
        producto.setCategorias(new LinkedHashSet<>(categorias));
        productoRepository.save(producto);
    }
}
