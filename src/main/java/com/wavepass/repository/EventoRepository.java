
package com.wavepass.repository;

import com.wavepass.model.Evento;
import com.wavepass.model.CategoriaEvento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    List<Evento> findByGeneroIgnoreCase(String genero);

    List<Evento> findByCategoria(CategoriaEvento categoria);

    Page<Evento> findByCriadoPorId(Long usuarioId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "sessoes")
    List<Evento> findAll(Specification<Evento> spec);
}
