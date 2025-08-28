package kr.co.authorizationtestrs.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.sql.DataSource;


@Configuration
public class AuthorizationServerConfig {
    @Value("${app.issuer}")
    private String issuer;

    /**
     * Authorization Server 필터 체인 (.well-known, /oauth2/*, OIDC endpoints 등)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain asSecurityFilterChain(HttpSecurity http) throws Exception {

        // Authorization Server Configurer
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                // Authorization Server 엔드포인트만 보안 매칭
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))

                // apply() 대신 with() 사용 (Spring Security 6.2+ 권장)
                .with(authorizationServerConfigurer, Customizer.withDefaults())

                // 인증 실패 시 로그인 페이지로 이동
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login")
                ));

        // OIDC 지원 추가
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        return http.build();


//        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http); // 이 부분이 핵심
//
//        http
//                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
//                .csrf(csrf -> csrf.disable()); // CSRF 설정은 필요에 따라 조정
//
//        // OIDC 활성화 (선택)
//        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
//                .oidc(Customizer.withDefaults());
//
//        return http.build();
        // OAuth2AuthorizationServerConfigurer를 with() 메서드를 통해 적용
//        http.with(new OAuth2AuthorizationServerConfigurer(), Customizer.withDefaults());
//
//        http
//                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
//                .csrf(csrf -> csrf.ignoringRequestMatchers(OAuth2AuthorizationServerConfigurer.authorizationServerEndpointsMatcher()));
//
//        // OIDC 활성화 (선택)
//        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
//                .oidc(Customizer.withDefaults());
//
//        return http.build();

//
//        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
//        // OIDC 활성화
//        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
//                .oidc(Customizer.withDefaults());
//        return http.build();
    }

    /**
     * RegisteredClient 저장소 (JDBC)
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(DataSource dataSource) {
        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
    }

    /**
     * Authorization/Consent 저장 (기본 Jdbc 구현)
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), clients);
    }

    @Bean
    public OAuth2AuthorizationConsentService consentService(DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), clients);
    }

    /**
     * JWT 인코더 (서명 키: JWK)
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * 토큰/클라이언트/프로바이더 설정
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuer) // http://localhost:9000
                .build();
    }
}
