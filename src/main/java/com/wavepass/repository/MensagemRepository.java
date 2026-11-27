package com.wavepass.repository;

import com.wavepass.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    @Query("select coalesce(max(m.ordem), 0) from Mensagem m where m.chat.id = :chatId")
    int buscarUltimaOrdem(@Param("chatId") Long chatId);
}
