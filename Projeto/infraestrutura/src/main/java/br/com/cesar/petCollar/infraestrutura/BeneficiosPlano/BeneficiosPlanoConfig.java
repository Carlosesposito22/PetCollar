package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConfigurarBeneficiosDoPlanoUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsultarBeneficiosTutorUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsumirBeneficioUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.GerarTicketBeneficioUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ProvisionarBeneficiosDoTutorUseCase;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.CalculoStatusBeneficioService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.ExpiracaoTicketService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.GeracaoTicketService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.ITicketBeneficioRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PublicadorDeAlteracoesBeneficio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.SincronizacaoBeneficioTutorObservador;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;

@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.BeneficiosPlano")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.BeneficiosPlano")
public class BeneficiosPlanoConfig {

    @Bean
    @Primary
    public IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio(BeneficioCatalogoRepositorioJpa repositorioJpa) {
        return new BeneficioCatalogoRepositorioProxy(repositorioJpa);
    }

    @Bean
    public CalculoStatusBeneficioService calculoStatusBeneficioService() {
        return new CalculoStatusBeneficioService();
    }

    @Bean
    public GeracaoTicketService geracaoTicketService(
            IBeneficioTutorRepositorio beneficioTutorRepositorio,
            ITicketBeneficioRepositorio ticketBeneficioRepositorio,
            CalculoStatusBeneficioService calculoStatusBeneficioService) {
        return new GeracaoTicketService(beneficioTutorRepositorio, ticketBeneficioRepositorio, calculoStatusBeneficioService);
    }

    @Bean
    public ExpiracaoTicketService expiracaoTicketService(
            ITicketBeneficioRepositorio ticketBeneficioRepositorio,
            IBeneficioTutorRepositorio beneficioTutorRepositorio) {
        return new ExpiracaoTicketService(ticketBeneficioRepositorio, beneficioTutorRepositorio);
    }

    @Bean
    public PublicadorDeAlteracoesBeneficio publicadorDeAlteracoesBeneficio(
            IBeneficioTutorRepositorio beneficioTutorRepositorio) {
        PublicadorDeAlteracoesBeneficio publicador = new PublicadorDeAlteracoesBeneficio();
        publicador.inscrever(new SincronizacaoBeneficioTutorObservador(beneficioTutorRepositorio));
        return publicador;
    }

    @Bean
    public ConsultarBeneficiosTutorUseCase consultarBeneficiosTutorUseCase(
            IBeneficioTutorRepositorio beneficioTutorRepositorio,
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
            CalculoStatusBeneficioService calculoStatusBeneficioService) {
        return new ConsultarBeneficiosTutorUseCase(
                beneficioTutorRepositorio, beneficioCatalogoRepositorio, calculoStatusBeneficioService);
    }

    @Bean
    public ConfigurarBeneficiosDoPlanoUseCase configurarBeneficiosDoPlanoUseCase(
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
            PublicadorDeAlteracoesBeneficio publicadorDeAlteracoesBeneficio) {
        return new ConfigurarBeneficiosDoPlanoUseCase(
                beneficioCatalogoRepositorio, publicadorDeAlteracoesBeneficio);
    }

    @Bean
    public ConsumirBeneficioUseCase consumirBeneficioUseCase(
            IBeneficioTutorRepositorio beneficioTutorRepositorio,
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio) {
        return new ConsumirBeneficioUseCase(beneficioTutorRepositorio, beneficioCatalogoRepositorio);
    }

    @Bean
    public ProvisionarBeneficiosDoTutorUseCase provisionarBeneficiosDoTutorUseCase(
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
            IBeneficioTutorRepositorio beneficioTutorRepositorio) {
        return new ProvisionarBeneficiosDoTutorUseCase(
                beneficioCatalogoRepositorio, beneficioTutorRepositorio);
    }

    @Bean
    public GerarTicketBeneficioUseCase gerarTicketBeneficioUseCase(
            IBeneficioTutorRepositorio beneficioTutorRepositorio,
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
            GeracaoTicketService geracaoTicketService,
            PublicadorDeEventosDoTutor publicadorDeEventosDoTutor) {
        return new GerarTicketBeneficioUseCase(
                beneficioTutorRepositorio, beneficioCatalogoRepositorio,
                geracaoTicketService, publicadorDeEventosDoTutor);
    }
}
