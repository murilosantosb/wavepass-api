package com.wavepass.config;

import com.jayway.jsonpath.JsonPath;
import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import com.wavepass.service.TokenService;
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

    @Autowired
    private TokenService tokenService;

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
                .andExpect(jsonPath("$.paths['/api/wavepass/usuario/registerClient'].post.security").isEmpty());
    }

    @Test
    void loginGeraTokenEConsultaEventosPermanecePublica() throws Exception {
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
                .andExpect(status().isOk());
        mvc.perform(get("/api/wavepass/evento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restringeConsultaEEdicaoDeContas() throws Exception {
        Usuario cliente = criarUsuario("cliente@example.com", PerfilUsuario.CLIENTE);
        Usuario outro = criarUsuario("outro@example.com", PerfilUsuario.CLIENTE);
        Usuario admin = criarUsuario("admin@example.com", PerfilUsuario.ADMIN);
        Usuario equipe = criarUsuario("equipe@example.com", PerfilUsuario.WAVEPASS);
        String token = "Bearer " + tokenService.gerarToken(cliente);
        String corpo = """
                {"nome":"Cliente atualizado","email":"cliente@example.com","senha":"NovaSenha123"}
                """;

        mvc.perform(get("/api/wavepass/usuario/" + outro.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden());
        for (Usuario alvo : java.util.List.of(outro, admin, equipe)) {
            mvc.perform(put("/api/wavepass/usuario/client/" + alvo.getId())
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/wavepass/usuario/" + cliente.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
        mvc.perform(put("/api/wavepass/usuario/client/" + cliente.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Cliente atualizado"));
        mvc.perform(put("/api/wavepass/usuario/client/" + admin.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.gerarToken(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/wavepass/usuario/" + outro.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.gerarToken(equipe)))
                .andExpect(status().isOk());
    }

    @Test
    void permiteCadastrosSemDocumentosERejeitaDuplicados() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/wavepass/usuario/registerClient")
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"nome":"Cliente","email":"novo%d@example.com","senha":"Senha123"}
                                    """.formatted(i)))
                    .andExpect(status().isCreated());
        }
        Usuario equipe = criarUsuario("cadastro-equipe@example.com", PerfilUsuario.WAVEPASS);
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/wavepass/usuario/registerAdmin")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.gerarToken(equipe))
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"nome":"Admin","nomeEmpresa":"Empresa","email":"empresa%d@example.com","senha":"Senha123"}
                                    """.formatted(i)))
                    .andExpect(status().isCreated());
        }
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/wavepass/usuario/registerClient")
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"nome":"Cliente","email":"cpf%d@example.com","cpf":"12345678901","senha":"Senha123"}
                                    """.formatted(i)))
                    .andExpect(status().is(i == 0 ? 201 : 400));
        }
    }

    @Test
    void cadastraEventoComEstadoESessoes() throws Exception {
        Usuario admin = criarUsuario("evento-admin@example.com", PerfilUsuario.ADMIN);
        String data = java.time.LocalDateTime.now().plusDays(10).withNano(0).toString();
        mvc.perform(post("/api/wavepass/evento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.gerarToken(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nome":"Show teste","genero":"Rock","categoria":"SHOW","faixaEtaria":0,"datas":["%s"],
                                 "local":"Arena","cidade":"Sao Paulo","estado":"SP","precoMinimo":80,
                                 "setores":[{"tipo":"PISTA","preco":80}]}
                                """.formatted(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("SP"))
                .andExpect(jsonPath("$.sessoes[0].dataHora").exists());
    }

    private Usuario criarUsuario(String email, PerfilUsuario perfil) {
        return usuarioRepository.save(Usuario.builder().nome("Usuario teste").email(email)
                .senha(passwordEncoder.encode("Senha123")).perfil(perfil).build());
    }
}
