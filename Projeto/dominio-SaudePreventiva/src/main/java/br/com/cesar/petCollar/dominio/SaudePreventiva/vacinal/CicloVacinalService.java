package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.FabricaDeProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.ICalculoProximaDoseStrategy;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço de domínio que orquestra o ciclo vacinal de um paciente (F-06).
 *
 * <p>Padrão Strategy: ao agendar a próxima dose, delega o cálculo da data
 * à estratégia selecionada pela {@link FabricaDeProtocolo} com base no
 * {@link TipoProtocolo} armazenado no {@link CicloVacinal}.
 * Cada protocolo (Filhote, Reforço Anual, Viagem, Personalizado) encapsula
 * seu próprio algoritmo de intervalo biológico (RN-075, RN-082).
 */
public class CicloVacinalService {

    private final ICicloVacinalRepositorio repositorio;

    public CicloVacinalService(ICicloVacinalRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("Repositório de ciclo vacinal não pode ser nulo.");
        this.repositorio = repositorio;
    }

    /**
     * Cria um novo ciclo vacinal com a primeira dose agendada.
     * O protocolo define qual estratégia será usada nas próximas previsões.
     */
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

    /**
     * Agenda a próxima dose de um ciclo existente.
     *
     * <p>Se {@code dataOverride} não for fornecida, usa o padrão Strategy:
     * a {@link FabricaDeProtocolo} seleciona a estratégia do protocolo do ciclo
     * e calcula automaticamente a data com base na última dose registrada (RN-075, RN-082).
     *
     * @param cicloId      id do ciclo vacinal
     * @param dataOverride data manual (prevalece sobre o cálculo da estratégia, se informada)
     * @return ciclo atualizado com a nova dose
     */
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

    /**
     * Confirma a aplicação de uma dose pelo veterinário (RN-078).
     */
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

    /**
     * Retorna a carteira completa de vacinação de um paciente (RN-072).
     */
    public List<CicloVacinal> listarPorPaciente(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        return repositorio.listarPorPaciente(pacienteId);
    }

    /**
     * Calcula a data sugerida para a próxima dose usando o padrão Strategy.
     * Seleciona a estratégia adequada ao protocolo do ciclo via {@link FabricaDeProtocolo}.
     */
    public LocalDate calcularProximaDataSugerida(CicloVacinal ciclo) {
        if (ciclo == null)
            throw new IllegalArgumentException("Ciclo vacinal não pode ser nulo.");
        ICalculoProximaDoseStrategy estrategia =
            FabricaDeProtocolo.criar(ciclo.getTipoProtocolo(), ciclo.getIntervaloDias());
        return ciclo.calcularProximaDataComEstrategia(estrategia);
    }

    /**
     * Verifica se o paciente possui algum ciclo vacinal com dose em atraso (RN-077).
     */
    public boolean possuiVacinaEmAtraso(PacienteId pacienteId) {
        return listarPorPaciente(pacienteId).stream()
                                            .anyMatch(CicloVacinal::possuiDoseEmAtraso);
    }

    /**
     * Remove todos os ciclos vacinais de um paciente (cascade ao remover paciente).
     */
    public void removerPorPaciente(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        repositorio.removerPorPaciente(pacienteId);
    }

    /**
     * Busca ciclo existente de um paciente pelo nome do ciclo.
     * Usado para agendar próxima dose por nome sem conhecer o id do ciclo.
     */
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
