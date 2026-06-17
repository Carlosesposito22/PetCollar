package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class Prescricao {

    private final PrescricaoId id;
    private final PacienteId pacienteId;
    private final TutorId tutorId;
    private final MedicoId medicoResponsavel;
    private final BigDecimal pesoPacienteKg;

    private final List<ItemPrescricao> itens;
    private final List<String> instrucoesGerais;
    private final Set<TagClinica> tagsClinicas;
    private final List<String> alergiasConsideradas;

    private StatusPrescricao status;
    private final AssinaturaDigitalPrescricao assinatura;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Prescricao(PrescricaoId id, PacienteId pacienteId, TutorId tutorId,
                      MedicoId medicoResponsavel, BigDecimal pesoPacienteKg,
                      List<ItemPrescricao> itens, List<String> instrucoesGerais,
                      Set<TagClinica> tagsClinicas, List<String> alergiasConsideradas,
                      MedicoId medicoQueAssina, String imagemAssinaturaBase64) {

        if (id == null)                throw new IllegalArgumentException("Id é obrigatório.");
        if (pacienteId == null)        throw new IllegalArgumentException("PacienteId é obrigatório.");
        if (tutorId == null)           throw new IllegalArgumentException("TutorId é obrigatório.");
        if (medicoResponsavel == null) throw new IllegalArgumentException("Médico responsável é obrigatório.");
        if (pesoPacienteKg == null || pesoPacienteKg.signum() <= 0)
            throw new IllegalArgumentException("Peso do paciente deve ser positivo.");
        if (itens == null || itens.isEmpty())
            throw new IllegalArgumentException("Prescrição deve ter pelo menos 1 item.");
        if (medicoQueAssina == null)
            throw new IllegalArgumentException("Médico que assina é obrigatório.");
        if (!medicoQueAssina.equals(medicoResponsavel))
            throw new IllegalStateException("Apenas o médico responsável pode assinar a prescrição.");
        if (imagemAssinaturaBase64 == null || imagemAssinaturaBase64.isBlank())
            throw new IllegalArgumentException("Assinatura é obrigatória.");

        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoResponsavel = medicoResponsavel;
        this.pesoPacienteKg = pesoPacienteKg;
        this.itens = Collections.unmodifiableList(new ArrayList<>(itens));
        this.instrucoesGerais = instrucoesGerais == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(instrucoesGerais));
        this.tagsClinicas = tagsClinicas == null || tagsClinicas.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(tagsClinicas));
        this.alergiasConsideradas = alergiasConsideradas == null
                ? List.of()
                : Collections.unmodifiableList(new LinkedHashSet<>(alergiasConsideradas).stream().toList());

        this.dataInicio = LocalDate.now();
        this.dataFim = this.dataInicio.plusDays(maiorDuracao());
        this.status = StatusPrescricao.FINALIZADA;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;

        String hash = AssinaturaDigitalPrescricao.calcularHash(resumoParaHash());
        this.assinatura = new AssinaturaDigitalPrescricao(
                medicoQueAssina, imagemAssinaturaBase64, LocalDateTime.now(), hash);
    }

    public Prescricao(PrescricaoId id, PacienteId pacienteId, TutorId tutorId,
                      MedicoId medicoResponsavel, BigDecimal pesoPacienteKg,
                      List<ItemPrescricao> itens, List<String> instrucoesGerais,
                      Set<TagClinica> tagsClinicas, List<String> alergiasConsideradas,
                      StatusPrescricao status, AssinaturaDigitalPrescricao assinatura,
                      LocalDate dataInicio, LocalDate dataFim,
                      LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoResponsavel = medicoResponsavel;
        this.pesoPacienteKg = pesoPacienteKg;
        this.itens = Collections.unmodifiableList(new ArrayList<>(itens));
        this.instrucoesGerais = instrucoesGerais == null
                ? List.of() : Collections.unmodifiableList(new ArrayList<>(instrucoesGerais));
        this.tagsClinicas = tagsClinicas == null || tagsClinicas.isEmpty()
                ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(tagsClinicas));
        this.alergiasConsideradas = alergiasConsideradas == null
                ? List.of() : Collections.unmodifiableList(new ArrayList<>(alergiasConsideradas));
        this.status = status;
        this.assinatura = assinatura;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void marcarComoSubstituida() {
        if (status != StatusPrescricao.FINALIZADA)
            throw new IllegalStateException(
                    "Só é possível substituir uma prescrição FINALIZADA. Estado atual: " + status);
        this.status = StatusPrescricao.SUBSTITUIDA;
        this.atualizadoEm = LocalDateTime.now();
    }

    private int maiorDuracao() {
        return itens.stream().mapToInt(ItemPrescricao::duracaoDias).max().orElse(0);
    }

    private String resumoParaHash() {
        StringBuilder sb = new StringBuilder()
                .append("prescricao=").append(id.getValor())
                .append("|paciente=").append(pacienteId.getValor())
                .append("|tutor=").append(tutorId.getValor())
                .append("|medico=").append(medicoResponsavel.getValor())
                .append("|peso=").append(pesoPacienteKg);
        for (ItemPrescricao i : itens)
            sb.append("|item=").append(i.medicamentoId().getValor())
              .append(':').append(i.doseMgPorKg())
              .append(':').append(i.duracaoDias());
        return sb.toString();
    }

    public PrescricaoId getId()                            { return id; }
    public PacienteId getPacienteId()                      { return pacienteId; }
    public TutorId getTutorId()                            { return tutorId; }
    public MedicoId getMedicoResponsavel()                 { return medicoResponsavel; }
    public BigDecimal getPesoPacienteKg()                  { return pesoPacienteKg; }
    public List<ItemPrescricao> getItens()                 { return itens; }
    public List<String> getInstrucoesGerais()              { return instrucoesGerais; }
    public Set<TagClinica> getTagsClinicas()               { return tagsClinicas; }
    public List<String> getAlergiasConsideradas()          { return alergiasConsideradas; }
    public StatusPrescricao getStatus()                    { return status; }
    public AssinaturaDigitalPrescricao getAssinatura()     { return assinatura; }
    public LocalDate getDataInicio()                       { return dataInicio; }
    public LocalDate getDataFim()                          { return dataFim; }
    public LocalDateTime getCriadoEm()                     { return criadoEm; }
    public LocalDateTime getAtualizadoEm()                 { return atualizadoEm; }
}
