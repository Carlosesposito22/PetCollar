package br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem")
public class RecepcaoTriagemConfig {
}
