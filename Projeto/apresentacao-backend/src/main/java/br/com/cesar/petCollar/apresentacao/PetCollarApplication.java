package br.com.cesar.petCollar.apresentacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstrap do petCollar. Escaneamento inclui todos os bounded contexts da
 * infraestrutura com JPA ativo e o pacote de apresentação (controllers, configs
 * de IdentidadeAcesso/PortalTutor e ACL fakes).
 *
 * <p>{@code @EnableScheduling} habilita o monitor de timeout do
 * ProtocoloInacessibilidade (RN 1) — ver {@code MonitorTimeoutTutorScheduler}.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "br.com.cesar.petCollar.apresentacao",
        "br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento",
        "br.com.cesar.petCollar.infraestrutura.AgendamentoClinico",
        "br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade",
        "br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem",
        "br.com.cesar.petCollar.infraestrutura.SaudePreventiva",
        "br.com.cesar.petCollar.infraestrutura.RelacaoTutor"
})
public class PetCollarApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCollarApplication.class, args);
    }
}
