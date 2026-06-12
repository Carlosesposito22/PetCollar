package br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.IMedicamentoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.InteracaoMedicamentosa;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ManejoAlimentar;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca.CalculadoraDoseMaximaSegura;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca.DoseMaximaBase;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca.RedutorPorAlergiaDecorator;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca.RedutorPorTagClinicaDecorator;

/**
 * Service de domínio que orquestra todas as camadas de validação da F-12:
 * <ol>
 *   <li>Monta a cadeia de Decorators (base → tag clínica → alergia) para
 *       calcular a dose máxima segura por item;</li>
 *   <li>Compara a dose proposta vs. máxima e emite violação BLOQUEIO se
 *       ultrapassar (RN 2);</li>
 *   <li>Confere via permitida (RN 1);</li>
 *   <li>Consulta a matriz de interações entre os itens (RN 4);</li>
 *   <li>Detecta conflitos de manejo alimentar nos horários (RN 7).</li>
 * </ol>
 */
public class ValidadorPrescricao {

    private final IMedicamentoRepositorio medicamentos;

    public ValidadorPrescricao(IMedicamentoRepositorio medicamentos) {
        if (medicamentos == null)
            throw new IllegalArgumentException("IMedicamentoRepositorio é obrigatório.");
        this.medicamentos = medicamentos;
    }

    public ResultadoValidacao validar(BigDecimal pesoPacienteKg,
                                     Set<TagClinica> tags,
                                     Set<String> alergias,
                                     List<RascunhoItem> itens) {
        if (pesoPacienteKg == null || pesoPacienteKg.signum() <= 0)
            throw new IllegalArgumentException("Peso do paciente deve ser positivo.");
        if (itens == null || itens.isEmpty())
            throw new IllegalArgumentException("Prescrição vazia.");

        List<Violacao> violacoes = new ArrayList<>();
        Map<MedicamentoId, ResultadoValidacao.DetalheItem> detalhes = new LinkedHashMap<>();

        List<MedicamentoId> idsParaInteracao = new ArrayList<>();

        // ── Camada por item: Decorator + verificações ──────────────────────
        for (RascunhoItem item : itens) {
            Medicamento med = medicamentos.buscarPorId(item.medicamentoId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Medicamento não encontrado no catálogo: " + item.medicamentoId()));

            // Empilha o Decorator: base → tag → alergia
            CalculadoraDoseMaximaSegura base = new DoseMaximaBase(med);
            RedutorPorTagClinicaDecorator comTag = new RedutorPorTagClinicaDecorator(base, tags);
            RedutorPorAlergiaDecorator comAlergia = new RedutorPorAlergiaDecorator(comTag, med, alergias);

            BigDecimal doseMaxSegura = comAlergia.calcular();
            BigDecimal doseTotal = item.doseMgPorKg().multiply(pesoPacienteKg).setScale(3, RoundingMode.HALF_UP);
            BigDecimal volume = med.getConcentracaoMgPorMl().signum() == 0
                    ? BigDecimal.ZERO
                    : doseTotal.divide(med.getConcentracaoMgPorMl(), 3, RoundingMode.HALF_UP);

            detalhes.put(med.getId(), new ResultadoValidacao.DetalheItem(
                    doseMaxSegura, doseTotal, volume,
                    comTag.foiAplicado(), comAlergia.foiAplicado()));

            if (comAlergia.foiAplicado()) {
                violacoes.add(Violacao.bloqueio("ALERGIA",
                        "Paciente é alérgico a componentes de " + med.getNome() + "."));
                continue; // próximas verificações deste item não fazem sentido
            }
            if (item.doseMgPorKg().compareTo(doseMaxSegura) > 0) {
                String reduzido = comTag.foiAplicado() ? " (já com redutor de 25% por tag clínica)" : "";
                violacoes.add(Violacao.bloqueio("SUPERDOSAGEM",
                        med.getNome() + ": dose proposta " + item.doseMgPorKg()
                                + " mg/kg ultrapassa o teto de segurança "
                                + doseMaxSegura + " mg/kg" + reduzido + "."));
            }
            if (!med.viaPermitida(item.via())) {
                violacoes.add(Violacao.bloqueio("VIA_INVALIDA",
                        med.getNome() + " não pode ser administrado por via " + item.via() + "."));
            }
            idsParaInteracao.add(med.getId());
        }

        // ── Camada cruzada: matriz de interação medicamentosa ──────────────
        for (InteracaoMedicamentosa inter : medicamentos.buscarInteracoesEntre(idsParaInteracao)) {
            String nomeA = medicamentos.buscarPorId(inter.medicamentoA()).map(Medicamento::getNome).orElse("?");
            String nomeB = medicamentos.buscarPorId(inter.medicamentoB()).map(Medicamento::getNome).orElse("?");
            String msg = nomeA + " + " + nomeB + ": " + inter.descricao();
            switch (inter.gravidade()) {
                case GRAVE -> violacoes.add(Violacao.bloqueio("INTERACAO_GRAVE", msg));
                case MODERADA -> violacoes.add(Violacao.alerta("INTERACAO_MODERADA", msg));
                case LEVE -> violacoes.add(Violacao.alerta("INTERACAO_LEVE", msg));
            }
        }

        // ── Camada cruzada: conflito de manejo alimentar (RN 7) ────────────
        List<Medicamento> jejum = new ArrayList<>();
        List<Medicamento> comAlimento = new ArrayList<>();
        for (RascunhoItem item : itens) {
            Medicamento med = medicamentos.buscarPorId(item.medicamentoId()).orElseThrow();
            if (med.getManejoAlimentar() == ManejoAlimentar.JEJUM) jejum.add(med);
            if (med.getManejoAlimentar() == ManejoAlimentar.COM_ALIMENTO) comAlimento.add(med);
        }
        if (!jejum.isEmpty() && !comAlimento.isEmpty()) {
            violacoes.add(Violacao.alerta("CONFLITO_MANEJO",
                    "Há medicamentos com manejo conflitante (jejum vs. com alimento). "
                            + "Considere separar os horários: "
                            + jejum.stream().map(Medicamento::getNome).toList()
                            + " (jejum) e "
                            + comAlimento.stream().map(Medicamento::getNome).toList()
                            + " (com alimento)."));
        }

        return new ResultadoValidacao(violacoes, detalhes);
    }
}
