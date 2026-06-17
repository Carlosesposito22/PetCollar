package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.ICalculoProximaDoseStrategy;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CicloVacinal {

    private final VacinaId id;
    private final PacienteId pacienteId;
    private final String nomeCiclo;
    private final int totalDoses;
    private final TipoProtocolo tipoProtocolo;
    private final Integer intervaloDias;
    private Integer diasLembrete;
    private final List<DoseVacinal> doses;

    public CicloVacinal(VacinaId id, PacienteId pacienteId, String nomeCiclo,
                        int totalDoses, TipoProtocolo tipoProtocolo, Integer intervaloDias) {
        if (id == null)
            throw new IllegalArgumentException("Id do ciclo vacinal não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (nomeCiclo == null || nomeCiclo.isBlank())
            throw new IllegalArgumentException("Nome do ciclo vacinal não pode ser vazio.");
        if (totalDoses < 1)
            throw new IllegalArgumentException("Total de doses deve ser pelo menos 1.");
        if (tipoProtocolo == null)
            throw new IllegalArgumentException("Tipo de protocolo não pode ser nulo.");
        if (tipoProtocolo == TipoProtocolo.PERSONALIZADO &&
                (intervaloDias == null || intervaloDias <= 0))
            throw new IllegalArgumentException("Intervalo em dias deve ser informado para protocolo personalizado.");
        this.id            = id;
        this.pacienteId    = pacienteId;
        this.nomeCiclo     = nomeCiclo.trim();
        this.totalDoses    = totalDoses;
        this.tipoProtocolo = tipoProtocolo;
        this.intervaloDias = intervaloDias;
        this.doses         = new ArrayList<>();
    }

    public CicloVacinal(VacinaId id, PacienteId pacienteId, String nomeCiclo,
                        int totalDoses, TipoProtocolo tipoProtocolo, Integer intervaloDias,
                        List<DoseVacinal> doses, Integer diasLembrete) {
        if (id == null)
            throw new IllegalArgumentException("Id do ciclo vacinal não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (nomeCiclo == null || nomeCiclo.isBlank())
            throw new IllegalArgumentException("Nome do ciclo vacinal não pode ser vazio.");
        if (totalDoses < 1)
            throw new IllegalArgumentException("Total de doses deve ser pelo menos 1.");
        if (tipoProtocolo == null)
            throw new IllegalArgumentException("Tipo de protocolo não pode ser nulo.");
        this.id            = id;
        this.pacienteId    = pacienteId;
        this.nomeCiclo     = nomeCiclo;
        this.totalDoses    = totalDoses;
        this.tipoProtocolo = tipoProtocolo;
        this.intervaloDias = intervaloDias;
        this.diasLembrete  = diasLembrete;
        this.doses         = doses != null ? new ArrayList<>(doses) : new ArrayList<>();
    }

    public void adicionarPrimeiraDose(LocalDate data) {
        if (!doses.isEmpty())
            throw new IllegalStateException("Este ciclo já possui a primeira dose registrada.");
        if (data == null)
            throw new IllegalArgumentException("Data da primeira dose não pode ser nula.");
        doses.add(new DoseVacinal(VacinaId.gerar(), 1, data));
    }

    public void adicionarProximaDose(LocalDate data) {
        if (doses.size() >= totalDoses)
            throw new IllegalStateException("Este ciclo já tem todas as doses planejadas.");
        if (data == null)
            throw new IllegalArgumentException("Data da dose não pode ser nula.");
        int proximoNumero = doses.size() + 1;
        doses.add(new DoseVacinal(VacinaId.gerar(), proximoNumero, data));
    }

    public void aplicarDose(VacinaId doseId, LocalDate dataAplicacao, String medico, String lote) {
        if (doseId == null)
            throw new IllegalArgumentException("Id da dose não pode ser nulo.");
        doses.stream()
             .filter(d -> d.getId().equals(doseId))
             .findFirst()
             .orElseThrow(() -> new IllegalArgumentException("Dose não encontrada neste ciclo."))
             .aplicar(dataAplicacao, medico, lote);
    }

    public void reagendarDose(VacinaId doseId, LocalDate novaData) {
        if (doseId == null)
            throw new IllegalArgumentException("Id da dose não pode ser nulo.");
        doses.stream()
             .filter(d -> d.getId().equals(doseId))
             .findFirst()
             .orElseThrow(() -> new IllegalArgumentException("Dose não encontrada neste ciclo."))
             .reagendar(novaData);
    }

    public LocalDate calcularProximaDataComEstrategia(ICalculoProximaDoseStrategy estrategia) {
        if (estrategia == null)
            throw new IllegalArgumentException("Estratégia de cálculo não pode ser nula.");
        return doses.stream()
                    .map(DoseVacinal::dataEfetiva)
                    .max(LocalDate::compareTo)
                    .map(estrategia::calcularProximaData)
                    .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma dose registrada no ciclo para calcular a próxima data."));
    }

    public void configurarLembrete(Integer diasLembrete) {
        if (diasLembrete != null && diasLembrete <= 0)
            throw new IllegalArgumentException("Dias de lembrete deve ser maior que zero.");
        this.diasLembrete = diasLembrete;
    }

    public boolean lembreteAtivo() {
        if (diasLembrete == null || diasLembrete <= 0) return false;
        LocalDate hoje = LocalDate.now();
        return doses.stream()
            .filter(d -> !d.estaAplicada())
            .map(DoseVacinal::getDataAgendada)
            .min(LocalDate::compareTo)
            .map(proxData -> {
                LocalDate inicio = proxData.minusDays(diasLembrete);
                return !hoje.isBefore(inicio) && !hoje.isAfter(proxData);
            })
            .orElse(false);
    }

    public boolean possuiDoseEmAtraso() {
        return doses.stream().anyMatch(DoseVacinal::estaEmAtraso);
    }

    public boolean estaConcluido() {
        return doses.stream().filter(DoseVacinal::estaAplicada).count() >= totalDoses;
    }

    public boolean podeAgendarProximaDose() {
        return doses.size() < totalDoses;
    }

    public int quantidadeAplicadas() {
        return (int) doses.stream().filter(DoseVacinal::estaAplicada).count();
    }

    public List<DoseVacinal> getDoses() {
        return Collections.unmodifiableList(doses);
    }

    public VacinaId getId()              { return id; }
    public PacienteId getPacienteId()    { return pacienteId; }
    public String getNomeCiclo()         { return nomeCiclo; }
    public int getTotalDoses()           { return totalDoses; }
    public TipoProtocolo getTipoProtocolo() { return tipoProtocolo; }
    public Integer getIntervaloDias()    { return intervaloDias; }
    public Integer getDiasLembrete()     { return diasLembrete; }
}
