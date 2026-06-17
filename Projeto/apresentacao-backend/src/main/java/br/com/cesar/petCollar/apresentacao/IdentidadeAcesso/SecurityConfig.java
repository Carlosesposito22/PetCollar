package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtAuthFiltro jwtAuthFiltro(JwtService jwt) {
        return new JwtAuthFiltro(jwt);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFiltro filtro, JwtProperties props) throws Exception {
        http
            .cors(c -> c.configurationSource(req -> {
                var cfg = new CorsConfiguration();

                cfg.setAllowedOriginPatterns(props.cors().origensPermitidas());
                cfg.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                cfg.setAllowedHeaders(java.util.List.of("*"));
                cfg.setAllowCredentials(true);
                var source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", cfg);
                return cfg;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/tutores/contratar").permitAll()
                .requestMatchers("/api/tutores/*/simular-pagamento").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/planos").permitAll()
                .requestMatchers("/api/tutor/indicacao/clique/**").permitAll()
                .requestMatchers("/api/tutor/indicacao/inscricao").permitAll()
                .requestMatchers("/api/tutor/indicacao/webhook/pagamento").permitAll()
                .requestMatchers("/api/especialidades/**").permitAll()
                .requestMatchers("/api/medicos/*/horarios-disponiveis").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN_CLINICA")
                .requestMatchers("/api/tutor/**").hasRole("TUTOR")
                .requestMatchers("/api/medico/**").hasRole("MEDICO_VETERINARIO")
                .requestMatchers("/api/recepcao/**").hasAnyRole("RECEPCIONISTA", "ADMIN_CLINICA")
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"mensagem\":\"Não autenticado.\"}");
                })
            )
            .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
