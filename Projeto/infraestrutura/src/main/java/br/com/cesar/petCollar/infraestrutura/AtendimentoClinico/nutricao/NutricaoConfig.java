package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CalcularNEMPreviewUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ConsultarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.FinalizarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.SalvarRascunhoPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CompararEvolucaoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ListarCatalogoRacoesUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.RecomendarRacoesUseCase;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao.AvaliacaoCorporalService;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao.ComparacaoEvolutivaNutricionalService;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.EstrategiaRecomendacaoRacao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.FaixaEtaria;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.IRacaoRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Porte;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RecomendacaoPorComorbidadeStrategy;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RecomendacaoPorFaixaEtariaStrategy;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RecomendacaoPorPorteStrategy;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RecomendacaoRacaoService;

/**
 * Wiring canônico (§6.5) do contexto F-11. Restringe a varredura JPA ao
 * subpacote {@code nutricao} para coexistir com outros agregados de
 * {@code AtendimentoClinico}.
 *
 * <p>A lista de {@link EstrategiaRecomendacaoRacao} fica concentrada aqui —
 * adicione novas Strategies pontuando em outros critérios e o
 * {@link RecomendacaoRacaoService} as combina automaticamente.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao")
public class NutricaoConfig {

    // ── Services de domínio ──────────────────────────────────────────────────

    @Bean
    public AvaliacaoCorporalService avaliacaoCorporalService() {
        return new AvaliacaoCorporalService();
    }

    @Bean
    public ComparacaoEvolutivaNutricionalService comparacaoEvolutivaNutricionalService() {
        return new ComparacaoEvolutivaNutricionalService();
    }

    @Bean
    public List<EstrategiaRecomendacaoRacao> estrategiasRecomendacaoRacao() {
        return List.of(
                new RecomendacaoPorComorbidadeStrategy(),
                new RecomendacaoPorFaixaEtariaStrategy(),
                new RecomendacaoPorPorteStrategy());
    }

    @Bean
    public RecomendacaoRacaoService recomendacaoRacaoService(
            IRacaoRepositorio repositorio,
            List<EstrategiaRecomendacaoRacao> estrategias) {
        return new RecomendacaoRacaoService(repositorio, estrategias);
    }

    // ── Use cases ────────────────────────────────────────────────────────────

    @Bean
    public CalcularNEMPreviewUseCase calcularNEMPreviewUseCase(AvaliacaoCorporalService avaliacao) {
        return new CalcularNEMPreviewUseCase(avaliacao);
    }

    @Bean
    public SalvarRascunhoPlanoNutricionalUseCase salvarRascunhoPlanoNutricionalUseCase(
            IPlanoNutricionalRepositorio repositorio) {
        return new SalvarRascunhoPlanoNutricionalUseCase(repositorio);
    }

    @Bean
    public FinalizarPlanoNutricionalUseCase finalizarPlanoNutricionalUseCase(
            IPlanoNutricionalRepositorio repositorio) {
        return new FinalizarPlanoNutricionalUseCase(repositorio);
    }

    @Bean
    public ConsultarPlanoNutricionalUseCase consultarPlanoNutricionalUseCase(
            IPlanoNutricionalRepositorio repositorio) {
        return new ConsultarPlanoNutricionalUseCase(repositorio);
    }

    @Bean
    public CompararEvolucaoNutricionalUseCase compararEvolucaoNutricionalUseCase(
            IPlanoNutricionalRepositorio repositorio,
            ComparacaoEvolutivaNutricionalService servico) {
        return new CompararEvolucaoNutricionalUseCase(repositorio, servico);
    }

    @Bean
    public RecomendarRacoesUseCase recomendarRacoesUseCase(RecomendacaoRacaoService servico) {
        return new RecomendarRacoesUseCase(servico);
    }

    @Bean
    public ListarCatalogoRacoesUseCase listarCatalogoRacoesUseCase(IRacaoRepositorio repositorio) {
        return new ListarCatalogoRacoesUseCase(repositorio);
    }

    // ── Seed inicial do catálogo (CLAUDE.md §6.5) ────────────────────────────

    @Bean
    public CommandLineRunner seedRacoes(IRacaoRepositorio repositorio) {
        return args -> {
            if (repositorio.contar() > 0) return; // idempotente

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Premier Pet", "Formula Filhote Raças Médias",
                    new BigDecimal("4100"),
                    EnumSet.of(FaixaEtaria.FILHOTE),
                    EnumSet.of(Porte.MEDIO, Porte.PEQUENO),
                    EnumSet.of(Comorbidade.NENHUMA)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Royal Canin", "Maxi Adult",
                    new BigDecimal("3850"),
                    EnumSet.of(FaixaEtaria.ADULTO),
                    EnumSet.of(Porte.GRANDE),
                    EnumSet.of(Comorbidade.NENHUMA)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Royal Canin", "Mini Adult",
                    new BigDecimal("3950"),
                    EnumSet.of(FaixaEtaria.ADULTO),
                    EnumSet.of(Porte.PEQUENO),
                    EnumSet.of(Comorbidade.NENHUMA)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Hill's", "Prescription Diet Metabolic",
                    new BigDecimal("3200"),
                    EnumSet.of(FaixaEtaria.ADULTO, FaixaEtaria.SENIOR),
                    EnumSet.allOf(Porte.class),
                    EnumSet.of(Comorbidade.OBESIDADE)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Royal Canin", "Diabetic",
                    new BigDecimal("3400"),
                    EnumSet.of(FaixaEtaria.ADULTO, FaixaEtaria.SENIOR),
                    EnumSet.allOf(Porte.class),
                    EnumSet.of(Comorbidade.DIABETES)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Hill's", "Prescription Diet k/d Renal",
                    new BigDecimal("3700"),
                    EnumSet.of(FaixaEtaria.ADULTO, FaixaEtaria.SENIOR),
                    EnumSet.allOf(Porte.class),
                    EnumSet.of(Comorbidade.DOENCA_RENAL)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "Premier Pet", "Senior Raças Médias e Grandes",
                    new BigDecimal("3550"),
                    EnumSet.of(FaixaEtaria.SENIOR),
                    EnumSet.of(Porte.MEDIO, Porte.GRANDE),
                    EnumSet.of(Comorbidade.NENHUMA)));

            repositorio.salvar(new Racao(RacaoId.gerar(),
                    "GranPlus", "Light Adulto",
                    new BigDecimal("3300"),
                    EnumSet.of(FaixaEtaria.ADULTO),
                    EnumSet.allOf(Porte.class),
                    EnumSet.of(Comorbidade.OBESIDADE, Comorbidade.NENHUMA)));
        };
    }
}
