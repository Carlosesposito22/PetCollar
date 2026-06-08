package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.ConfirmarPagamentoCobrancaUseCase;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.ConsultarResumoFinanceiroUseCase;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.ContratarPlanoUseCase;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.PlanosPadrao;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.IPlanoRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.ValorMensalidade;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico.ClassificacaoInadimplenciaService;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico.ConsolidacaoQuitacaoService;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;
/**
 * Wiring canônico (§6.5) dos services de domínio e use cases de F-07 como beans.
 * Spring resolve as interfaces {@code IXxxRepositorio} para os adapters JPA
 * deste mesmo módulo.
 *
 * <p>Inclui um {@link CommandLineRunner} que semeia o "Plano Básico Mensal" no
 * primeiro boot — o id do plano é fixo (UUID determinístico) para permitir que
 * outros tutores (seed do PortalTutor, contratações via API) referenciem o
 * mesmo registro sem precisar consultar.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento")
public class AssinaturaFaturamentoConfig {

    // ── Services de domínio ──────────────────────────────────────────────────

    @Bean
    public ClassificacaoInadimplenciaService classificacaoInadimplenciaService(
            ICobrancaRepositorio cobrancaRepositorio) {
        return new ClassificacaoInadimplenciaService(cobrancaRepositorio);
    }

    @Bean
    public ConsolidacaoQuitacaoService consolidacaoQuitacaoService(
            ICobrancaRepositorio cobrancaRepositorio) {
        return new ConsolidacaoQuitacaoService(cobrancaRepositorio);
    }

    // ── Use cases ────────────────────────────────────────────────────────────

    @Bean
    public ContratarPlanoUseCase contratarPlanoUseCase(
            IPlanoRepositorio planoRepositorio, ICobrancaRepositorio cobrancaRepositorio) {
        return new ContratarPlanoUseCase(planoRepositorio, cobrancaRepositorio);
    }

    @Bean
    public ConfirmarPagamentoCobrancaUseCase confirmarPagamentoCobrancaUseCase(
            ICobrancaRepositorio cobrancaRepositorio,
            PublicadorDeEventosDoTutor publicadorDeEventosDoTutor) {
        return new ConfirmarPagamentoCobrancaUseCase(cobrancaRepositorio, publicadorDeEventosDoTutor);
    }

    @Bean
    public ConsultarResumoFinanceiroUseCase consultarResumoFinanceiroUseCase(
            ICobrancaRepositorio cobrancaRepositorio,
            IPlanoRepositorio planoRepositorio,
            ClassificacaoInadimplenciaService classificacaoInadimplenciaService) {
        return new ConsultarResumoFinanceiroUseCase(
                cobrancaRepositorio, planoRepositorio, classificacaoInadimplenciaService);
    }

    // ── Seed de planos ───────────────────────────────────────────────────────

    @Bean
    public CommandLineRunner seedPlanos(IPlanoRepositorio planoRepositorio) {
        return args -> {
            if (planoRepositorio.buscarPorId(PlanosPadrao.ID_PLANO_BASICO_MENSAL).isEmpty()) {
                planoRepositorio.salvar(new Plano(
                        PlanosPadrao.ID_PLANO_BASICO_MENSAL,
                        PlanosPadrao.NOME_PLANO_BASICO_MENSAL,
                        ValorMensalidade.de(PlanosPadrao.VALOR_PLANO_BASICO_MENSAL)
                ));
            }
        };
    }

}
