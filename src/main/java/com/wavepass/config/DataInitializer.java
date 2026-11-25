package com.wavepass.config;

import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${api.security.master.email}")
    private String masterEmail;

    @Value("${api.security.master.password}")
    private String masterPassword;

    @Override
    public void run(String... args) {
        if (masterEmail == null || masterEmail.isBlank() || masterPassword == null || masterPassword.isBlank()) {
            return;
        }
        if (!usuarioRepository.existsByEmail(masterEmail)) {
            Usuario wavepassMaster = Usuario.builder()
                    .nome("Equipe Wavepass")
                    .email(masterEmail)
                    .senha(passwordEncoder.encode(masterPassword))
                    .perfil(PerfilUsuario.WAVEPASS)
                    .build();

            usuarioRepository.save(wavepassMaster);
        }
    }
}