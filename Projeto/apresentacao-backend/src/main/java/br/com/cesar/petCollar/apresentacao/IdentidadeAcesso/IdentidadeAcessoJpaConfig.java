package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.PlanosPadrao;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.apresentacao.IdentidadeAcesso")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.apresentacao.IdentidadeAcesso")
public class IdentidadeAcessoJpaConfig {

    private static final Logger log = LoggerFactory.getLogger(IdentidadeAcessoJpaConfig.class);

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

    @Bean
    public CommandLineRunner seedTutorPadrao(UsuarioJpaRepository usuarios,
                                              TutorRecepcaoJpaRepository tutoresRecepcao,
                                              PasswordEncoder encoder) {
        return args -> {
            String email = "tutor@petcollar.com";
            String cpf   = "12345678900";
            String nome  = "Tutor Padrão";
            String planoId = PlanosPadrao.ID_PLANO_BASICO_MENSAL.getValor();

            if (usuarios.findByEmail(email).isEmpty()) {
                String senhaHash = encoder.encode("petcollar123");
                UsuarioJpa tutor = UsuarioJpa.fromDomain(new UsuarioAutenticavel(
                    email, nome, Perfil.TUTOR, senhaHash, StatusConta.ATIVA,
                    cpf, null, null, email, planoId));
                usuarios.save(tutor);
                log.info("[SEED] Tutor-padrão criado: {} / petcollar123", email);
            }

            if (!tutoresRecepcao.existsByCpf(cpf)) {
                String uuid = UUID.nameUUIDFromBytes(("seed-tutor-" + cpf).getBytes()).toString();
                tutoresRecepcao.save(new TutorRecepcaoJpa(uuid, nome, cpf, null, email, LocalDateTime.now()));
                log.info("[SEED] Tutor-padrão adicionado à recepção (CPF: {}).", cpf);
            }
        };
    }
}
