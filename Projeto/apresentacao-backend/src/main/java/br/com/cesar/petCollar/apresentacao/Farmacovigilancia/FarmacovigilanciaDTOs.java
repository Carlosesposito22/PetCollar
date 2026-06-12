package br.com.cesar.petCollar.apresentacao.Farmacovigilancia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.InteracaoMedicamentosa;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.AssinaturaDigitalPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.ItemPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.ResultadoValidacao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.Violacao;

public final class FarmacovigilanciaDTOs {

    private FarmacovigilanciaDTOs() {}

    // ── Contexto do paciente ─────────────────────────────────────────────────

    public record ContextoPacienteDTO(
            String pacienteId, String tutorId,
            String nomePet, String nomeTutor,
            BigDecimal pesoPacienteKg, int idadeAnos,
            List<String> alergiasDoPaciente,
            List<String> tagsClinicasDerivadas) {}

    // ── Catálogo de medicamentos ─────────────────────────────────────────────

    public record MedicamentoDTO(
            String id, String nome,
            BigDecimal doseMaximaMgPorKg, BigDecimal concentracaoMgPorMl,
            List<String> viasPermitidas, List<String> componentes,
            String manejoAlimentar, String notaCuidado) {
        public static MedicamentoDTO de(Medicamento m) {
            return new MedicamentoDTO(
                    m.getId().getValor(), m.getNome(),
                    m.getDoseMaximaMgPorKg(), m.getConcentracaoMgPorMl(),
                    m.getViasPermitidas().stream().map(Enum::name).toList(),
                    m.getComponentes().stream().toList(),
                    m.getManejoAlimentar().name(), m.getNotaCuidado());
        }
    }

    public record InteracaoDTO(String medicamentoAId, String medicamentoBId, String gravidade, String descricao) {
        public static InteracaoDTO de(InteracaoMedicamentosa i) {
            return new InteracaoDTO(
                    i.medicamentoA().getValor(), i.medicamentoB().getValor(),
                    i.gravidade().name(), i.descricao());
        }
    }

    // ── Templates ────────────────────────────────────────────────────────────

    public record TemplateDTO(String id, String nome, String descricao, List<TemplateItemDTO> itens) {
        public static TemplateDTO de(TemplatePrescricao t) {
            return new TemplateDTO(
                    t.getId().getValor(), t.getNome(), t.getDescricao(),
                    t.getItens().stream().map(TemplateItemDTO::de).toList());
        }
    }

    public record TemplateItemDTO(
            String medicamentoId, BigDecimal doseMgPorKg, int duracaoDias,
            String frequencia, String via) {
        public static TemplateItemDTO de(TemplatePrescricao.ItemTemplate it) {
            return new TemplateItemDTO(
                    it.medicamentoId().getValor(), it.doseMgPorKg(), it.duracaoDias(),
                    it.frequencia().name(), it.via().name());
        }
    }

    // ── Entrada: validar ─────────────────────────────────────────────────────

    public record RequisicaoRascunhoItemDTO(
            String medicamentoId, BigDecimal doseMgPorKg, int duracaoDias,
            String frequencia, String via) {}

    public record RequisicaoValidarDTO(
            BigDecimal pesoPacienteKg,
            List<String> tagsClinicas,
            List<String> alergias,
            List<RequisicaoRascunhoItemDTO> itens) {}

    // ── Saída: resultado de validação ────────────────────────────────────────

    public record ViolacaoDTO(String nivel, String codigo, String mensagem) {
        public static ViolacaoDTO de(Violacao v) {
            return new ViolacaoDTO(v.nivel().name(), v.codigo(), v.mensagem());
        }
    }

    public record DetalheItemDTO(
            String medicamentoId,
            BigDecimal doseMaximaSeguraCalculada,
            BigDecimal doseTotalPropostaMg,
            BigDecimal volumeFinalMl,
            boolean tagAplicada,
            boolean alergiaAplicada) {}

    public record ResultadoValidacaoDTO(
            boolean podeFinalizar,
            List<ViolacaoDTO> violacoes,
            List<DetalheItemDTO> detalhes) {
        public static ResultadoValidacaoDTO de(ResultadoValidacao r) {
            List<DetalheItemDTO> detalhes = r.detalhePorItem().entrySet().stream()
                    .map(e -> new DetalheItemDTO(
                            e.getKey().getValor(),
                            e.getValue().doseMaximaSeguraCalculada(),
                            e.getValue().doseTotalPropostaMg(),
                            e.getValue().volumeFinalMl(),
                            e.getValue().tagAplicada(),
                            e.getValue().alergiaAplicada()))
                    .toList();
            return new ResultadoValidacaoDTO(
                    r.podeFinalizar(),
                    r.violacoes().stream().map(ViolacaoDTO::de).toList(),
                    detalhes);
        }
    }

    // ── Entrada: criar+finalizar ─────────────────────────────────────────────

    public record RequisicaoItemFinalDTO(
            String medicamentoId, BigDecimal doseMgPorKg, int duracaoDias,
            String frequencia, String via, List<String> horarios, String notaCuidado) {}

    public record RequisicaoFinalizarDTO(
            String pacienteId, String tutorId,
            BigDecimal pesoPacienteKg,
            List<String> tagsClinicas,
            List<String> alergias,
            List<RequisicaoItemFinalDTO> itens,
            List<String> instrucoesGerais,
            String imagemAssinaturaBase64) {}

    // ── Saída: Prescrição finalizada ─────────────────────────────────────────

    public record ItemPrescricaoDTO(
            String medicamentoId, String nomeMedicamento,
            BigDecimal doseMgPorKg, BigDecimal doseTotalMg, BigDecimal volumeFinalMl,
            int duracaoDias, String frequencia, String via,
            List<String> horarios, String notaCuidado) {
        public static ItemPrescricaoDTO de(ItemPrescricao it) {
            return new ItemPrescricaoDTO(
                    it.medicamentoId().getValor(), it.nomeMedicamento(),
                    it.doseMgPorKg(), it.doseTotalMg(), it.volumeFinalMl(),
                    it.duracaoDias(), it.frequencia().name(), it.via().name(),
                    it.horarios().stream().map(h -> h.valor()).toList(),
                    it.notaCuidado());
        }
    }

    public record AssinaturaDTO(String medicoResponsavelId, String imagemBase64,
                                 LocalDateTime assinadoEm, String hashConteudo) {
        public static AssinaturaDTO de(AssinaturaDigitalPrescricao a) {
            return new AssinaturaDTO(a.medicoResponsavel().getValor(),
                    a.imagemBase64(), a.assinadoEm(), a.hashConteudo());
        }
    }

    public record PrescricaoDTO(
            String id, String pacienteId, String tutorId, String medicoResponsavelId,
            BigDecimal pesoPacienteKg,
            List<ItemPrescricaoDTO> itens,
            List<String> instrucoesGerais,
            List<String> tagsClinicas,
            List<String> alergiasConsideradas,
            String status,
            AssinaturaDTO assinatura,
            LocalDate dataInicio, LocalDate dataFim,
            LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        public static PrescricaoDTO de(Prescricao p) {
            return new PrescricaoDTO(
                    p.getId().getValor(),
                    p.getPacienteId().getValor(),
                    p.getTutorId().getValor(),
                    p.getMedicoResponsavel().getValor(),
                    p.getPesoPacienteKg(),
                    p.getItens().stream().map(ItemPrescricaoDTO::de).toList(),
                    p.getInstrucoesGerais(),
                    p.getTagsClinicas().stream().map(TagClinica::name).toList(),
                    p.getAlergiasConsideradas(),
                    p.getStatus().name(),
                    AssinaturaDTO.de(p.getAssinatura()),
                    p.getDataInicio(), p.getDataFim(),
                    p.getCriadoEm(), p.getAtualizadoEm());
        }
    }

    /** Histórico do paciente: cada prescrição + map de id→nome dos medicamentos pra UI exibir. */
    public record HistoricoDTO(List<PrescricaoDTO> prescricoes, Map<String, String> nomesDosMedicamentos) {}
}
