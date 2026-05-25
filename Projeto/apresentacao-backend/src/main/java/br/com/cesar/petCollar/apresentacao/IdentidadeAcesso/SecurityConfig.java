package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

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
                // Patterns (não setAllowedOrigins) porque a configuração pode conter curingas
                // e usamos allowCredentials=true (Spring só permite curinga via patterns).
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
                .requestMatchers("/api/admin/**").hasRole("ADMIN_CLINICA")
                .anyRequest().authenticated()
            )
            .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
