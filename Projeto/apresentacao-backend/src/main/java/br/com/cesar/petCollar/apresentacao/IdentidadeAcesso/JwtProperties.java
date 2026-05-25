package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "petcollar.security")
public record JwtProperties(Jwt jwt, Cors cors) {

    public record Jwt(String secret, String issuer, long expiracaoMinutos) {}
    public record Cors(List<String> origensPermitidas) {}
}
