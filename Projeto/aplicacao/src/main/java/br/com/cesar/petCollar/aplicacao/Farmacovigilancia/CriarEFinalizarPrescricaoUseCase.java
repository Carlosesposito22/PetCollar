package br.com.cesar.petCollar.aplicacao.Farmacovigilancia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.IMedicamentoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.HorarioAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.IPrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.ItemPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.PrescricaoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.RascunhoItem;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.ResultadoValidacao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.ValidadorPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.Violacao;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class CriarEFinalizarPrescricaoUseCase {

    private final IMedicamentoRepositorio medicamentos;
    private final IPrescricaoRepositorio prescricoes;
    private final ValidadorPrescricao validador;

    public CriarEFinalizarPrescricaoUseCase(IMedicamentoRepositorio medicamentos,
                                            IPrescricaoRepositorio prescricoes,
                                            ValidadorPrescricao validador) {
        if (medicamentos == null) throw new IllegalArgumentException("IMedicamentoRepositorio é obrigatório.");
        if (prescricoes == null)  throw new IllegalArgumentException("IPrescricaoRepositorio é obrigatório.");
        if (validador == null)    throw new IllegalArgumentException("ValidadorPrescricao é obrigatório.");
        this.medicamentos = medicamentos;
        this.prescricoes = prescricoes;
        this.validador = validador;
    }

    public Prescricao executar(Entrada entrada) {
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");

        ResultadoValidacao resultado = validador.validar(
                entrada.pesoPacienteKg, entrada.tags, entrada.alergias, entrada.rascunhos);

        if (!resultado.podeFinalizar()) {
            String motivos = resultado.violacoes().stream()
                    .filter(v -> v.nivel() == Violacao.Nivel.BLOQUEIO)
                    .map(Violacao::mensagem).reduce((a, b) -> a + " | " + b).orElse("");
            throw new IllegalStateException("Prescrição não pode ser finalizada: " + motivos);
        }

        List<ItemPrescricao> itens = new ArrayList<>();
        for (DadosItem dados : entrada.itensComHorarios) {
            Medicamento med = medicamentos.buscarPorId(dados.medicamentoId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Medicamento não encontrado: " + dados.medicamentoId));
            List<HorarioAdministracao> horarios = dados.horarios.stream()
                    .map(HorarioAdministracao::new).toList();
            itens.add(ItemPrescricao.calcular(
                    med.getId(), med.getNome(),
                    dados.doseMgPorKg, entrada.pesoPacienteKg, med.getConcentracaoMgPorMl(),
                    dados.duracaoDias, dados.frequencia, dados.via,
                    horarios,
                    dados.notaCuidado == null ? med.getNotaCuidado() : dados.notaCuidado));
        }

        prescricoes.buscarVigenteDoPaciente(entrada.pacienteId)
                .ifPresent(anterior -> {
                    anterior.marcarComoSubstituida();
                    prescricoes.salvar(anterior);
                });

        Prescricao nova = new Prescricao(
                PrescricaoId.gerar(),
                entrada.pacienteId, entrada.tutorId, entrada.medicoResponsavel,
                entrada.pesoPacienteKg,
                itens, entrada.instrucoesGerais, entrada.tags, new ArrayList<>(entrada.alergias),
                entrada.medicoResponsavel, entrada.imagemAssinaturaBase64);

        prescricoes.salvar(nova);
        return nova;
    }

    public record Entrada(
            PacienteId pacienteId,
            TutorId tutorId,
            MedicoId medicoResponsavel,
            BigDecimal pesoPacienteKg,
            Set<TagClinica> tags,
            Set<String> alergias,
            List<RascunhoItem> rascunhos,
            List<DadosItem> itensComHorarios,
            List<String> instrucoesGerais,
            String imagemAssinaturaBase64
    ) {}

    public record DadosItem(
            MedicamentoId medicamentoId,
            BigDecimal doseMgPorKg,
            int duracaoDias,
            Frequencia frequencia,
            ViaAdministracao via,
            List<String> horarios,
            String notaCuidado
    ) {}
}
