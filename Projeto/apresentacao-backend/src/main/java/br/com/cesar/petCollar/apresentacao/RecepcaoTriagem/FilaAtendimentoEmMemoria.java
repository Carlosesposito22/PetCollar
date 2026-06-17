package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class FilaAtendimentoEmMemoria {

    private static final Map<String, Integer> PRIORIDADE =
        Map.of("VERMELHO", 0, "AMARELO", 1, "VERDE", 2);

    private final ItemFilaJpaRepository repo;

    public FilaAtendimentoEmMemoria(ItemFilaJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public List<ItemFilaDTO> inserir(ItemFila item) {
        repo.deleteByPacienteId(item.pacienteId());
        repo.save(toJpa(item));
        return listar();
    }

    @Transactional
    public boolean encaminhar(String triagemId, String medicoId, String nomeMedico) {
        return repo.findById(triagemId).map(jpa -> {
            jpa.setMedicoId(medicoId);
            jpa.setNomeMedico(nomeMedico);
            repo.save(jpa);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void remover(String triagemId) {
        repo.deleteById(triagemId);
    }

    @Transactional
    public void removerPorPaciente(String pacienteId) {
        repo.deleteByPacienteId(pacienteId);
    }

    public boolean contemPaciente(String pacienteId) {
        return repo.existsByPacienteId(pacienteId);
    }

    public List<ItemFilaDTO> listar() {
        return repo.findAll().stream()
            .sorted(Comparator
                .comparing(ItemFilaJpa::isAplicacaoVacina)
                .thenComparingInt(i -> PRIORIDADE.getOrDefault(i.getCorDeRisco(), 3))
                .thenComparing(ItemFilaJpa::getFinalizadaEm))
            .map(this::toDTO)
            .toList();
    }

    public List<ItemFilaDTO> listarPorMedico(String medicoId) {
        return repo.findByMedicoId(medicoId).stream().map(this::toDTO).toList();
    }

    private ItemFilaJpa toJpa(ItemFila i) {
        return new ItemFilaJpa(
            i.triagemId(), i.pacienteId(), i.corDeRisco(), i.finalizadaEm(),
            i.nomePaciente(), i.tutorId(), i.medicoId(), i.nomeMedico(),
            i.aplicacaoVacina());
    }

    private ItemFilaDTO toDTO(ItemFilaJpa j) {
        return new ItemFilaDTO(
            j.getPacienteId(), j.getTriagemId(), j.getCorDeRisco(),
            j.getFinalizadaEm(), j.getNomePaciente(), j.getTutorId(),
            j.getMedicoId(), j.getNomeMedico(), j.isAplicacaoVacina());
    }

    public record ItemFila(
        String pacienteId,
        String triagemId,
        String corDeRisco,
        LocalDateTime finalizadaEm,
        String nomePaciente,
        String tutorId,
        String medicoId,
        String nomeMedico,
        boolean aplicacaoVacina
    ) {
        public ItemFila(String pacienteId, String triagemId, String corDeRisco,
                        LocalDateTime finalizadaEm, String nomePaciente, String tutorId,
                        boolean aplicacaoVacina) {
            this(pacienteId, triagemId, corDeRisco, finalizadaEm,
                 nomePaciente, tutorId, null, null, aplicacaoVacina);
        }
    }

    public record ItemFilaDTO(
        String pacienteId,
        String triagemId,
        String corDeRisco,
        LocalDateTime finalizadaEm,
        String nomePaciente,
        String tutorId,
        String medicoId,
        String nomeMedico,
        boolean aplicacaoVacina
    ) {
        public static ItemFilaDTO de(ItemFila i) {
            return new ItemFilaDTO(
                i.pacienteId(), i.triagemId(), i.corDeRisco(),
                i.finalizadaEm(), i.nomePaciente(), i.tutorId(),
                i.medicoId(), i.nomeMedico(), i.aplicacaoVacina());
        }
    }
}
