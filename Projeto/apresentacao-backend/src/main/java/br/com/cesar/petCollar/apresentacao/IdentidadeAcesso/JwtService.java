package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey chave;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.chave = Keys.hmacShaKeyFor(props.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenEmitido emitirPara(UsuarioAutenticavel u) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(props.jwt().expiracaoMinutos(), ChronoUnit.MINUTES);

        String token = Jwts.builder()
                .issuer(props.jwt().issuer())
                .subject(u.identificador())
                .claim("perfil", u.perfil().name())
                .claim("nome", u.nome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expira))
                .signWith(chave)
                .compact();

        return new TokenEmitido(token, expira.toEpochMilli());
    }

    public Claims validar(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(chave)
                .requireIssuer(props.jwt().issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record TokenEmitido(String token, long expiraEm) {}
}
