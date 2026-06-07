package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.ICicloVacinalRepositorio;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Wiring canônico (§6.5) dos beans do contexto Saúde Preventiva.
 * Ativa o scan de entidades e repositórios JPA deste pacote.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.SaudePreventiva")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.SaudePreventiva")
public class SaudePreventivConfig {

    @Bean
    public CicloVacinalService cicloVacinalService(ICicloVacinalRepositorio repositorio) {
        return new CicloVacinalService(repositorio);
    }
}
