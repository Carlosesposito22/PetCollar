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

/**
 * Wiring canônico (§6.5) dos services de domínio de F-08 como beans. Spring
 * resolve as interfaces {@code IXxxRepositorio} para os adapters JPA deste
 * mesmo módulo.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.BeneficiosPlano")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.BeneficiosPlano")
public class BeneficiosPlanoConfig {

    // ── Proxy de cache sobre o catálogo de benefícios (padrão Proxy, §8) ─────

    /**
     * Sobrepõe o adapter JPA com um proxy de cache (CLAUDE.md §8: "para
     * sobrepor um bean... use @Primary"): o catálogo é dado de referência, lido
     * a cada cálculo de status e alterado raramente, só pelo admin.
     */
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

    // ── Observer: publicador de alterações de catálogo (padrão Observer, §8) ─

    /**
     * Já nasce com o {@link SincronizacaoBeneficioTutorObservador} inscrito
     * (CLAUDE.md §6.5). O {@code AlterarConfiguracaoBeneficioUseCase} (Fase 5)
     * injetará este bean e chamará {@code publicar(catalogo)} após persistir a
     * alteração — sem precisar conhecer a lógica de sincronização.
     */
    @Bean
    public PublicadorDeAlteracoesBeneficio publicadorDeAlteracoesBeneficio(
            IBeneficioTutorRepositorio beneficioTutorRepositorio) {
        PublicadorDeAlteracoesBeneficio publicador = new PublicadorDeAlteracoesBeneficio();
        publicador.inscrever(new SincronizacaoBeneficioTutorObservador(beneficioTutorRepositorio));
        return publicador;
    }

    // ── Casos de uso do Tutor ────────────────────────────────────────────────

    @Bean
    public ConsultarBeneficiosTutorUseCase consultarBeneficiosTutorUseCase(
            IBeneficioTutorRepositorio beneficioTutorRepositorio,
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
            CalculoStatusBeneficioService calculoStatusBeneficioService) {
        return new ConsultarBeneficiosTutorUseCase(
                beneficioTutorRepositorio, beneficioCatalogoRepositorio, calculoStatusBeneficioService);
    }

    // ── Casos de uso de administração de benefícios do plano (F-08) ──────────

    @Bean
    public ConfigurarBeneficiosDoPlanoUseCase configurarBeneficiosDoPlanoUseCase(
            IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
            PublicadorDeAlteracoesBeneficio publicadorDeAlteracoesBeneficio) {
        return new ConfigurarBeneficiosDoPlanoUseCase(
                beneficioCatalogoRepositorio, publicadorDeAlteracoesBeneficio);
    }

    /**
     * Orquestra o consumo/devolução de benefícios a partir de outros subdomínios
     * (F-05 consultas, F-06 vacinação). Injetado nos controllers de agendamento
     * e vacinação para gatear cada serviço pelo benefício do plano.
     */
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
