package com.wavepass.config;

import com.jayway.jsonpath.JsonPath;
import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AutenticacaoIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private FilterChainProxy springSecurityFilterChain;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain).build();
    }

    @Test
    void swaggerAssociaBearerAsRotasProtegidas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/wavepass/usuario/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/wavepass/usuario/register'].post.security").isEmpty());
    }

    @Test
    void loginGeraTokenQueLiberaRotaProtegida() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Teste JWT").email("jwt.integration@example.com")
                .senha(passwordEncoder.encode("SenhaExclusivaTeste123!"))
                .telefone("11999999999").cpf("12345678901")
                .dataNascimento(LocalDate.of(2000, 1, 1))
                .perfil(PerfilUsuario.CLIENTE).build());

        var login = mvc.perform(post("/api/wavepass/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"jwt.integration@example.com","senha":"SenhaExclusivaTeste123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        mvc.perform(get("/api/wavepass/evento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/wavepass/evento"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/wavepass/evento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }
}
