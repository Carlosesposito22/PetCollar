package br.com.cesar.petCollar.apresentacao.PortalTutor;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Ativa o mapeamento JPA para Paciente e Vacina do PortalTutor. Os tipos
 * vivem em apresentacao-backend enquanto os bounded contexts
 * dominio-RelacaoTutor / dominio-SaudePreventiva não expõem esses agregados.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.apresentacao.PortalTutor")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.apresentacao.PortalTutor")
public class PortalTutorJpaConfig {
}
