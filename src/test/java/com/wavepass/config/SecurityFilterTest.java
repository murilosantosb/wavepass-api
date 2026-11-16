package com.wavepass.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.wavepass.model.Usuario;
import com.wavepass.service.AuthService;
import com.wavepass.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityFilterTest {

    private static final String SECRET = "chave-exclusiva-para-testes-de-autenticacao";
    private final TokenService tokenService = new TokenService();
    private final AuthService authService = mock(AuthService.class);
    private final SecurityFilter filter = new SecurityFilter(tokenService, authService);
    private final Usuario usuario = Usuario.builder().email("cliente@example.com").build();

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokenGeradoNoLoginAutenticaUsuario() throws Exception {
        when(authService.loadUserByUsername(usuario.getEmail())).thenReturn(usuario);
        var request = requestComToken(tokenService.gerarToken(usuario));
        var response = new MockHttpServletResponse();
        boolean[] executou = {false};

        filter.doFilter(request, response, (req, res) -> {
            executou[0] = true;
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication.isAuthenticated()).isTrue();
            assertThat(authentication.getPrincipal()).isSameAs(usuario);
            assertThat(authentication.getCredentials()).isNull();
            assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        });

        assertThat(executou[0]).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void requisicaoSemTokenContinuaSemAutenticacao() throws Exception {
        boolean[] executou = {false};
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {
            executou[0] = true;
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        });
        assertThat(executou[0]).isTrue();
        verifyNoInteractions(authService);
    }

    @Test
    void rejeitaTokensInvalidosExpiradosOuSemClaimsObrigatorias() throws Exception {
        var algorithm = Algorithm.HMAC256(SECRET);
        var validade = Instant.now().plusSeconds(3600);
        var tokens = List.of(
                "token-invalido",
                "",
                JWT.create().withIssuer("API Wavepass").withSubject(usuario.getEmail())
                        .withExpiresAt(validade).sign(Algorithm.HMAC256("outra-chave")),
                JWT.create().withIssuer("API Wavepass").withSubject(usuario.getEmail())
                        .withExpiresAt(Instant.now().minusSeconds(60)).sign(algorithm),
                JWT.create().withIssuer("outro-emissor").withSubject(usuario.getEmail())
                        .withExpiresAt(validade).sign(algorithm),
                JWT.create().withIssuer("API Wavepass").withExpiresAt(validade).sign(algorithm),
                JWT.create().withIssuer("API Wavepass").withSubject(usuario.getEmail()).sign(algorithm)
        );

        for (String token : tokens) {
            var response = new MockHttpServletResponse();
            filter.doFilter(requestComToken(token), response, (req, res) -> {
                throw new AssertionError("Token invalido nao pode acessar o endpoint");
            });
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
        verifyNoInteractions(authService);
    }

    @Test
    void rejeitaTokenDeUsuarioRemovido() throws Exception {
        when(authService.loadUserByUsername(usuario.getEmail()))
                .thenThrow(new UsernameNotFoundException(usuario.getEmail()));
        var response = new MockHttpServletResponse();

        filter.doFilter(requestComToken(tokenService.gerarToken(usuario)), response, (req, res) -> {
            throw new AssertionError("Usuario removido nao pode acessar o endpoint");
        });

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private MockHttpServletRequest requestComToken(String token) {
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return request;
    }
}
