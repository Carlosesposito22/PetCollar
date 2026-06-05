package br.com.cesar.petCollar.apresentacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Bootstrap do petCollar. Amplia o escaneamento padrão para incluir só os
 * subpacotes da infraestrutura que JÁ estão prontos para JPA. Os módulos em
 * memória dos demais colegas (AgendamentoClinico, etc.) continuam ativos via
 * pacotes em {@code apresentacao}; serão migrados individualmente.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "br.com.cesar.petCollar.apresentacao",
        "br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento"
})
public class PetCollarApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCollarApplication.class, args);
    }
}
