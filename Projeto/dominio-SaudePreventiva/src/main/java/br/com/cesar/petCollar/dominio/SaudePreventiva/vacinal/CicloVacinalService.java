package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.FabricaDeProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.ICalculoProximaDoseStrategy;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDate;
import java.util.List;

public class CicloVacinalService {

    private final ICicloVacinalRepositorio repositorio;

    public CicloVacinalService(ICicloVacinalRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("Repositório de ciclo vacinal não pode ser nulo.");
        this.repositorio = repositorio;
    }

    public CicloVacinal criarCicloComPrimeiraDose(PacienteId pacienteId, String nomeCiclo,
                                                   TipoProtocolo protocolo, int totalDoses,
                                                   Integer intervaloDias, LocalDate primeiraData) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (nomeCiclo == null || nomeCiclo.isBlank())
            throw new IllegalArgumentException("Nome do ciclo vacinal não pode ser vazio.");
        if (primeiraData == null)
            throw new IllegalArgumentException("Data da primeira dose não pode ser nula.");

        CicloVacinal ciclo = new CicloVacinal(
            VacinaId.gerar(), pacienteId, nomeCiclo, totalDoses, protocolo, intervaloDias);
        ciclo.adicionarPrimeiraDose(primeiraData);
        repositorio.salvar(ciclo);
        return ciclo;
    }

    public CicloVacinal agendarProximaDose(VacinaId cicloId, LocalDate dataOverride) {
        if (cicloId == null)
            throw new IllegalArgumentException("Id do ciclo não pode ser nulo.");

        CicloVacinal ciclo = repositorio.buscarPorId(cicloId)
            .orElseThrow(() -> new IllegalArgumentException("Ciclo vacinal não encontrado: " + cicloId));

        if (!ciclo.podeAgendarProximaDose())
            throw new IllegalStateException("Este ciclo já tem todas as doses planejadas.");

        LocalDate dataFinal = dataOverride != null
            ? dataOverride
            : calcularProximaDataSugerida(ciclo);

        ciclo.adicionarProximaDose(dataFinal);
        repositorio.salvar(ciclo);
        return ciclo;
    }

    public void aplicarDose(VacinaId cicloId, VacinaId doseId,
                             LocalDate dataAplicacao, String medico, String lote) {
        if (cicloId == null)
            throw new IllegalArgumentException("Id do ciclo não pode ser nulo.");
        if (doseId == null)
            throw new IllegalArgumentException("Id da dose não pode ser nulo.");

        CicloVacinal ciclo = repositorio.buscarPorId(cicloId)
            .orElseThrow(() -> new IllegalArgumentException("Ciclo vacinal não encontrado: " + cicloId));

        ciclo.aplicarDose(doseId, dataAplicacao, medico, lote);
        repositorio.salvar(ciclo);
    }

    public void reagendarDose(VacinaId cicloId, VacinaId doseId, LocalDate novaData) {
        if (cicloId == null)
            throw new IllegalArgumentException("Id do ciclo não pode ser nulo.");
        if (doseId == null)
            throw new IllegalArgumentException("Id da dose não pode ser nulo.");

        CicloVacinal ciclo = repositorio.buscarPorId(cicloId)
            .orElseThrow(() -> new IllegalArgumentException("Ciclo vacinal não encontrado: " + cicloId));

        ciclo.reagendarDose(doseId, novaData);
        repositorio.salvar(ciclo);
    }

    public List<CicloVacinal> listarPorPaciente(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        return repositorio.listarPorPaciente(pacienteId);
    }

    public LocalDate calcularProximaDataSugerida(CicloVacinal ciclo) {
        if (ciclo == null)
            throw new IllegalArgumentException("Ciclo vacinal não pode ser nulo.");
        ICalculoProximaDoseStrategy estrategia =
            FabricaDeProtocolo.criar(ciclo.getTipoProtocolo(), ciclo.getIntervaloDias());
        return ciclo.calcularProximaDataComEstrategia(estrategia);
    }

    public void configurarLembrete(VacinaId cicloId, Integer diasLembrete) {
        if (cicloId == null)
            throw new IllegalArgumentException("Id do ciclo não pode ser nulo.");
        CicloVacinal ciclo = repositorio.buscarPorId(cicloId)
            .orElseThrow(() -> new IllegalArgumentException("Ciclo vacinal não encontrado: " + cicloId));
        ciclo.configurarLembrete(diasLembrete);
        repositorio.salvar(ciclo);
    }

    public boolean possuiVacinaEmAtraso(PacienteId pacienteId) {
        return listarPorPaciente(pacienteId).stream()
                                            .anyMatch(CicloVacinal::possuiDoseEmAtraso);
    }

    public void removerCiclo(VacinaId cicloId) {
        if (cicloId == null)
            throw new IllegalArgumentException("Id do ciclo não pode ser nulo.");
        repositorio.buscarPorId(cicloId)
            .orElseThrow(() -> new IllegalArgumentException("Ciclo vacinal não encontrado: " + cicloId));
        repositorio.remover(cicloId);
    }

    public void removerPorPaciente(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        repositorio.removerPorPaciente(pacienteId);
    }

    public CicloVacinal buscarCicloPorNome(PacienteId pacienteId, String nomeCiclo) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (nomeCiclo == null || nomeCiclo.isBlank())
            throw new IllegalArgumentException("Nome do ciclo não pode ser vazio.");
        return repositorio.buscarPorPacienteENomeCiclo(pacienteId, nomeCiclo.trim())
            .orElseThrow(() -> new IllegalArgumentException(
                "Ciclo '" + nomeCiclo + "' não encontrado. Use 'Agendar nova vacina'."));
    }
}
