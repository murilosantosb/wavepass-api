package com.wavepass.repository;

import com.wavepass.model.Chat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    boolean existsByIdAndUsuario_Id(Long id, Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Chat> findByIdAndUsuario_Id(Long id, Long usuarioId);
}
