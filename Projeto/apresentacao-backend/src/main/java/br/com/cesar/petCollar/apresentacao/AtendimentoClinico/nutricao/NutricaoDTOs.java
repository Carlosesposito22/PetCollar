package br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CalcularNEMPreviewUseCase;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao.AvaliacaoCorporal;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.AssinaturaDigital;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.CronogramaTransicao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.DiaTransicao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ObservacaoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ParametrosPaciente;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ResultadoNEM;

/**
 * Container de DTOs (records) do contexto F-11 — agrupados para evitar 1
 * arquivo por record. Entrada usa o prefixo {@code Requisicao}; saída usa o
 * sufixo {@code DTO} com factory estático {@code de(...)}.
 */
public final class NutricaoDTOs {

    private NutricaoDTOs() {}

    // ── Entrada ──────────────────────────────────────────────────────────────

    public record RequisicaoParametrosDTO(
            BigDecimal pesoAtualKg,
            BigDecimal pesoIdealKg,
            String nivelAtividade,
            String comorbidade,
            BigDecimal densidadeCaloricaKcalPorKg) {}

    public record RequisicaoDiaTransicaoDTO(
            String faixaDias, int percentualRacaoAtual, int percentualRacaoNova) {
        DiaTransicao paraDominio() {
            return new DiaTransicao(faixaDias, percentualRacaoAtual, percentualRacaoNova);
        }
    }

    public record RequisicaoCronogramaDTO(String tipo, List<RequisicaoDiaTransicaoDTO> dias) {
        CronogramaTransicao paraDominio() {
            var tipoCron = br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.TipoCronograma
                    .valueOf(tipo);
            return new CronogramaTransicao(tipoCron, dias.stream().map(RequisicaoDiaTransicaoDTO::paraDominio).toList());
        }
    }

    public record RequisicaoPreviewNEMDTO(RequisicaoParametrosDTO parametros) {}

    public record RequisicaoRascunhoDTO(
            String pacienteId,
            String tutorId,
            RequisicaoParametrosDTO parametros,
            RequisicaoCronogramaDTO cronograma,
            List<String> observacoes,
            String racaoId,
            String justificativaDivergencia) {}

    public record RequisicaoFinalizarDTO(String imagemAssinaturaBase64) {}

    /**
     * Carga atômica: tudo que o plano precisa + assinatura num único POST.
     * Usada pelo botão "Finalizar e Assinar" (não passa por rascunho).
     */
    public record RequisicaoCriarEFinalizarDTO(
            String pacienteId,
            String tutorId,
            RequisicaoParametrosDTO parametros,
            RequisicaoCronogramaDTO cronograma,
            List<String> observacoes,
            String racaoId,
            String justificativaDivergencia,
            String imagemAssinaturaBase64) {}

    // ── Saída ────────────────────────────────────────────────────────────────

    public record ParametrosDTO(
            BigDecimal pesoAtualKg, BigDecimal pesoIdealKg,
            String nivelAtividade, String comorbidade,
            BigDecimal densidadeCaloricaKcalPorKg) {
        public static ParametrosDTO de(ParametrosPaciente p) {
            return new ParametrosDTO(
                    p.pesoAtualKg(), p.pesoIdealKg(),
                    p.nivelAtividade().name(), p.comorbidade().name(),
                    p.densidadeCaloricaKcalPorKg());
        }
    }

    public record ResultadoNEMDTO(
            BigDecimal pesoMetabolico, BigDecimal nemBase, BigDecimal fatorAtividade,
            BigDecimal modificadorComorbidade, BigDecimal nemTotal,
            BigDecimal quantidadeRecomendadaGramasPorDia) {
        public static ResultadoNEMDTO de(ResultadoNEM r) {
            return new ResultadoNEMDTO(
                    r.pesoMetabolico(), r.nemBase(), r.fatorAtividade(),
                    r.modificadorComorbidade(), r.nemTotal(),
                    r.quantidadeRecomendadaGramasPorDia());
        }
    }

    public record AvaliacaoCorporalDTO(String classificacao, BigDecimal divergenciaPercentual, boolean exigeAlerta) {
        public static AvaliacaoCorporalDTO de(AvaliacaoCorporal a) {
            return new AvaliacaoCorporalDTO(a.classificacao().name(), a.divergenciaPercentual(), a.exigeAlerta());
        }
    }

    public record PreviewNEMDTO(ResultadoNEMDTO nem, AvaliacaoCorporalDTO avaliacaoCorporal) {
        public static PreviewNEMDTO de(CalcularNEMPreviewUseCase.Resultado r) {
            return new PreviewNEMDTO(ResultadoNEMDTO.de(r.nem()), AvaliacaoCorporalDTO.de(r.avaliacaoCorporal()));
        }
    }

    public record DiaTransicaoDTO(String faixaDias, int percentualRacaoAtual, int percentualRacaoNova) {
        public static DiaTransicaoDTO de(DiaTransicao d) {
            return new DiaTransicaoDTO(d.faixaDias(), d.percentualRacaoAtual(), d.percentualRacaoNova());
        }
    }

    public record CronogramaDTO(String tipo, List<DiaTransicaoDTO> dias) {
        public static CronogramaDTO de(CronogramaTransicao c) {
            return new CronogramaDTO(c.tipo().name(), c.dias().stream().map(DiaTransicaoDTO::de).toList());
        }
    }

    public record AssinaturaDigitalDTO(String medicoResponsavelId, String imagemBase64,
                                       LocalDateTime assinadoEm, String hashConteudo) {
        public static AssinaturaDigitalDTO de(AssinaturaDigital a) {
            return new AssinaturaDigitalDTO(a.medicoResponsavel().getValor(),
                    a.imagemBase64(), a.assinadoEm(), a.hashConteudo());
        }
    }

    public record PlanoNutricionalDTO(
            String id, String pacienteId, String tutorId, String medicoResponsavelId,
            ParametrosDTO parametros, CronogramaDTO cronograma,
            List<String> observacoes, String status,
            LocalDateTime criadoEm, LocalDateTime atualizadoEm,
            ResultadoNEMDTO resultadoFinalizado, AssinaturaDigitalDTO assinatura,
            String racaoId, String justificativaDivergencia,
            BigDecimal divergenciaPercentual) {

        public static PlanoNutricionalDTO de(PlanoNutricional p) {
            return new PlanoNutricionalDTO(
                    p.getId().getValor(),
                    p.getPacienteId().getValor(),
                    p.getTutorId().getValor(),
                    p.getMedicoResponsavel().getValor(),
                    ParametrosDTO.de(p.getParametros()),
                    CronogramaDTO.de(p.getCronograma()),
                    p.getObservacoes().stream().map(ObservacaoNutricional::texto).toList(),
                    p.getStatus().name(),
                    p.getCriadoEm(), p.getAtualizadoEm(),
                    p.getResultadoFinalizado() == null ? null : ResultadoNEMDTO.de(p.getResultadoFinalizado()),
                    p.getAssinatura() == null ? null : AssinaturaDigitalDTO.de(p.getAssinatura()),
                    p.getRacaoId() == null ? null : p.getRacaoId().getValor(),
                    p.getJustificativaDivergencia(),
                    p.divergenciaPercentual());
        }
    }

    // ── DTOs do catálogo de ração e da recomendação ──────────────────────────

    public record RacaoDTO(
            String id, String fabricante, String linha, String descricaoCurta,
            BigDecimal densidadeCaloricaKcalPorKg,
            List<String> faixasIndicadas, List<String> portesIndicados, List<String> comorbidadesIndicadas) {
        public static RacaoDTO de(br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao r) {
            return new RacaoDTO(
                    r.getId().getValor(), r.getFabricante(), r.getLinha(), r.descricaoCurta(),
                    r.getDensidadeCaloricaKcalPorKg(),
                    r.getFaixasIndicadas().stream().map(Enum::name).toList(),
                    r.getPortesIndicados().stream().map(Enum::name).toList(),
                    r.getComorbidadesIndicadas().stream().map(Enum::name).toList());
        }
    }

    public record RacaoRecomendadaDTO(RacaoDTO racao, int pontuacao, java.util.Map<String, Integer> detalhes, List<String> motivosFortes) {
        public static RacaoRecomendadaDTO de(br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoRecomendada r) {
            return new RacaoRecomendadaDTO(RacaoDTO.de(r.racao()), r.pontuacao(), r.detalhes(), r.motivosFortes());
        }
    }

    // ── DTOs da evolução nutricional ────────────────────────────────────────

    public record EvolucaoNutricionalDTO(
            LocalDateTime planoAnteriorEm, LocalDateTime planoAtualEm,
            BigDecimal pesoAtualAnteriorKg, BigDecimal pesoAtualNovoKg,
            BigDecimal deltaPesoKg, BigDecimal deltaPesoPercentual,
            BigDecimal nemAnteriorKcal, BigDecimal nemNovoKcal,
            BigDecimal deltaNemPercentual, String tendenciaPeso) {
        public static EvolucaoNutricionalDTO de(
                br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao.EvolucaoNutricional e) {
            return new EvolucaoNutricionalDTO(
                    e.planoAnteriorEm(), e.planoAtualEm(),
                    e.pesoAtualAnteriorKg(), e.pesoAtualNovoKg(),
                    e.deltaPesoKg(), e.deltaPesoPercentual(),
                    e.nemAnteriorKcal(), e.nemNovoKcal(),
                    e.deltaNemPercentual(), e.tendenciaPeso().name());
        }
    }

    /**
     * Contexto mínimo do paciente para a tela de Gestão Nutricional —
     * resolve o tutorId real e os dados básicos a partir das tabelas já
     * existentes (sem mexer em endpoints de colegas).
     */
    public record ContextoPacienteDTO(
            String pacienteId, String tutorId,
            String nomePet, String nomeTutor,
            BigDecimal pesoAtualKg, int idadeAnos) {}

    public record HistoricoEvolutivoDTO(List<PlanoNutricionalDTO> historico, List<EvolucaoNutricionalDTO> evolucoes) {
        public static HistoricoEvolutivoDTO de(
                br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CompararEvolucaoNutricionalUseCase.Resultado r) {
            return new HistoricoEvolutivoDTO(
                    r.historico().stream().map(PlanoNutricionalDTO::de).toList(),
                    r.evolucoes().stream().map(EvolucaoNutricionalDTO::de).toList());
        }
    }

    // ── Conversor compartilhado entrada → domínio ────────────────────────────

    public static ParametrosPaciente paraDominio(RequisicaoParametrosDTO dto) {
        return new ParametrosPaciente(
                dto.pesoAtualKg(), dto.pesoIdealKg(),
                br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.NivelAtividade
                        .valueOf(dto.nivelAtividade()),
                br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade
                        .valueOf(dto.comorbidade()),
                dto.densidadeCaloricaKcalPorKg());
    }
}
