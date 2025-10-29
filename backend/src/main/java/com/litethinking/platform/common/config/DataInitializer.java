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
import java.util.Set;

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

            Empresa empresa = empresaRepository.findById("900123456")
                    .orElseGet(() -> empresaRepository.save(new Empresa("900123456", "Lite Thinking", "Calle 1 #2-3", "+57 312 000 1111")));

            Categoria categoria = categoriaRepository.findByNombre("Tecnologia")
                    .orElseGet(() -> categoriaRepository.save(new Categoria("Tecnologia")));

            if (productoRepository.findByEmpresaNit(empresa.getNit()).isEmpty()) {
                Producto laptop = new Producto("PROD-001", "Laptop Pro", "Laptop de 16GB RAM", empresa);
                ProductoPrecio precioCop = new ProductoPrecio("COP", new BigDecimal("4500000"));
                ProductoPrecio precioUsd = new ProductoPrecio("USD", new BigDecimal("1100"));
                precioCop.setProducto(laptop);
                precioUsd.setProducto(laptop);
                laptop.replacePrecios(Set.of(precioCop, precioUsd));
                laptop.setCategorias(Set.of(categoria));
                productoRepository.save(laptop);
            }
        };
    }
}
