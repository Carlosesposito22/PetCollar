package br.com.cesar.petCollar.apresentacao.PortalTutor;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.apresentacao.PortalTutor")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.apresentacao.PortalTutor")
public class PortalTutorJpaConfig {
}
