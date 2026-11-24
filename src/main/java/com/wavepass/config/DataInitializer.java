package com.wavepass.config;

import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String emailMaster = "admin@wavepass.com";

        if (!usuarioRepository.existsByEmail(emailMaster)) {
            Usuario wavepassMaster = Usuario.builder()
                    .nome("Equipe Wavepass")
                    .email(emailMaster)
                    .senha(passwordEncoder.encode("Wavepass@2026"))
                    .perfil(PerfilUsuario.WAVEPASS)
                    .build();

            usuarioRepository.save(wavepassMaster);
        }
    }
}