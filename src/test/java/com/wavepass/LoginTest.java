package com.wavepass;

import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:login;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class LoginTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void aceitaCredenciaisValidas() throws Exception {
        when(usuarioRepository.findByEmail("teste@example.com")).thenReturn(Usuario.builder()
                .email("teste@example.com").senha(passwordEncoder.encode("Senha123456")).build());
        login("{\"email\":\"teste@example.com\",\"senha\":\"Senha123456\"}", 200);
    }

    @Test
    void rejeitaSenhaIncorretaEUsuarioInexistente() throws Exception {
        when(usuarioRepository.findByEmail("teste@example.com")).thenReturn(Usuario.builder()
                .email("teste@example.com").senha(passwordEncoder.encode("Senha123456")).build());
        login("{\"email\":\"teste@example.com\",\"senha\":\"errada\"}", 401);
        login("{\"email\":\"inexistente@example.com\",\"senha\":\"errada\"}", 401);
    }

    @Test
    void rejeitaCamposInvalidos() throws Exception {
        login("{}", 400);
        login("{\"email\":\"invalido\",\"senha\":\"123456\"}", 400);
        login("{\"email\":\"teste@example.com\",\"senha\":\" \"}", 400);
    }

    private void login(String body, int expectedStatus) throws Exception {
        mvc.perform(post("/api/wavepass/usuario/login").contentType("application/json").content(body))
                .andExpect(status().is(expectedStatus));
    }
}
