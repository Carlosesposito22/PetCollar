package br.com.cesar.petCollar.apresentacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "br.com.cesar.petCollar.apresentacao",
        "br.com.cesar.petCollar.infraestrutura.AtendimentoClinico",
        "br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento",
        "br.com.cesar.petCollar.infraestrutura.AgendamentoClinico",
        "br.com.cesar.petCollar.infraestrutura.BeneficiosPlano",
        "br.com.cesar.petCollar.infraestrutura.Gamificacao",
        "br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade",
        "br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem",
        "br.com.cesar.petCollar.infraestrutura.SaudePreventiva",
        "br.com.cesar.petCollar.infraestrutura.RelacaoTutor",
        "br.com.cesar.petCollar.infraestrutura.Farmacovigilancia"
})
public class PetCollarApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCollarApplication.class, args);
    }
}
