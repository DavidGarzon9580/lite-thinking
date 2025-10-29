package com.litethinking.platform.catalog.service;

import com.litethinking.platform.catalog.domain.Categoria;
import com.litethinking.platform.catalog.dto.CategoriaRequest;
import com.litethinking.platform.catalog.dto.CategoriaResponse;
import com.litethinking.platform.catalog.repository.CategoriaRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        categoriaRepository.findByNombre(request.nombre()).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException("La categoría ya existe");
        });

        Categoria categoria = new Categoria(request.nombre());
        Categoria saved = categoriaRepository.save(categoria);
        return new CategoriaResponse(saved.getId(), saved.getNombre());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .map(categoria -> new CategoriaResponse(categoria.getId(), categoria.getNombre()))
                .toList();
    }
}
