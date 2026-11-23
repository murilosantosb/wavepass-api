package com.wavepass.repository;

import com.wavepass.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    List<Evento> findByGeneroIgnoreCase(String genero);

    List<Evento> findByCategoriaIgnoreCase(String categoria);

    List<Evento> findByNomeContainingIgnoreCaseOrArtistasContainingIgnoreCase(String nome, String artistas);
}