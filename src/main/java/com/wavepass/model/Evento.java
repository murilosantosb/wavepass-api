package com.wavepass.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String artistas;

    private String genero;
    private String categoria;
    private int faixaEtaria;
    private LocalDateTime dataHora;
    private String local;
    private BigDecimal precoMinimo;
    private String corTema;
    private String imagemBanner;
    private String imagemCard;

    @Column(columnDefinition = "TEXT")
    private String descricaoCurta;

    @Column(columnDefinition = "TEXT")
    private String descricaoCompleta;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "evento_id")
    @Builder.Default
    private List<Setor> setores = new ArrayList<>();
}