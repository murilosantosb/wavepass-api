package com.wavepass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagem")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Mensagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Length.LONG32)
    private String conteudo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMensagem tipo;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private Integer ordem;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
}
