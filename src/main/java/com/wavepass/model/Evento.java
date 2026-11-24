package com.wavepass.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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

    @Column(nullable = false)
    private String nome;

    private String artistas;

    @Column(nullable = false)
    private String genero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaEvento categoria;

    private int faixaEtaria;

    @Column(nullable = false)
    private String local;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String estado;

    private BigDecimal precoMinimo;
    private String corTema;

    @Column(columnDefinition = "TEXT")
    private String imagemBanner;

    @Column(columnDefinition = "TEXT")
    private String imagemCard;

    @Column(columnDefinition = "TEXT")
    private String descricaoCurta;

    @Column(columnDefinition = "TEXT")
    private String descricaoCompleta;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "evento_id")
    @Builder.Default
    private List<SessaoEvento> sessoes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "evento_id")
    @Builder.Default
    private List<Setor> setores = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;
}