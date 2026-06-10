package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Ativa o mapeamento JPA para o contexto IdentidadeAcesso. Os tipos de domínio
 * (Perfil, StatusConta, UsuarioAutenticavel, UsuarioRepositorio) vivem neste
 * módulo enquanto dominio-IdentidadeAcesso não é formalizado; a camada JPA
 * reside aqui por consequência.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.apresentacao.IdentidadeAcesso")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.apresentacao.IdentidadeAcesso")
public class IdentidadeAcessoJpaConfig {

    private static final Logger log = LoggerFactory.getLogger(IdentidadeAcessoJpaConfig.class);

    /**
     * Seed operacional: garante que o administrador-raiz exista e com a senha-padrão
     * correta. Usa upsert para corrigir automaticamente casos em que o hash ficou
     * desatualizado entre recriações do banco ou mudança de configuração do encoder.
     */
    @Bean
    public CommandLineRunner seedAdminPadrao(UsuarioJpaRepository usuarios,
                                              PasswordEncoder encoder) {
        return args -> {
            String senhaHash = encoder.encode("petcollar123");
            UsuarioJpa admin = UsuarioJpa.fromDomain(new UsuarioAutenticavel(
                    "admin@petcollar.com", "Administrador",
                    Perfil.ADMIN_CLINICA,
                    senhaHash,
                    StatusConta.ATIVA));
            usuarios.save(admin);
            log.info("[SEED] Admin-raiz garantido: admin@petcollar.com / petcollar123");
        };
    }
}
