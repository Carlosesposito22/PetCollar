package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class FilaAtendimentoEmMemoria {

    private final CopyOnWriteArrayList<ItemFila> fila = new CopyOnWriteArrayList<>();

    private static final Map<String, Integer> PRIORIDADE = Map.of(
        "VERMELHO", 0, "AMARELO", 1, "VERDE", 2);

    public List<ItemFilaDTO> inserir(ItemFila item) {
        fila.removeIf(i -> i.pacienteId().equals(item.pacienteId()));
        fila.add(item);
        reordenar();
        return listar();
    }

    /** Encaminha um item já existente na fila para um médico específico. */
    public boolean encaminhar(String triagemId, String medicoId, String nomeMedico) {
        for (int i = 0; i < fila.size(); i++) {
            ItemFila atual = fila.get(i);
            if (atual.triagemId().equals(triagemId)) {
                fila.set(i, new ItemFila(
                    atual.pacienteId(), atual.triagemId(), atual.corDeRisco(),
                    atual.finalizadaEm(), atual.nomePaciente(), atual.tutorId(),
                    medicoId, nomeMedico));
                return true;
            }
        }
        return false;
    }

    public void remover(String triagemId) {
        fila.removeIf(i -> i.triagemId().equals(triagemId));
    }

    /** Fila completa (usada pela recepcionista). */
    public List<ItemFilaDTO> listar() {
        return fila.stream().map(ItemFilaDTO::de).toList();
    }

    /** Apenas itens encaminhados para um médico específico. */
    public List<ItemFilaDTO> listarPorMedico(String medicoId) {
        return fila.stream()
            .filter(i -> medicoId.equals(i.medicoId()))
            .map(ItemFilaDTO::de)
            .toList();
    }

    private void reordenar() {
        fila.sort(Comparator
            .comparingInt((ItemFila i) -> PRIORIDADE.getOrDefault(i.corDeRisco(), 3))
            .thenComparing(ItemFila::finalizadaEm));
    }

    public record ItemFila(
        String pacienteId,
        String triagemId,
        String corDeRisco,
        LocalDateTime finalizadaEm,
        String nomePaciente,
        String tutorId,
        String medicoId,      // null enquanto não encaminhado
        String nomeMedico
    ) {
        /** Construtor de criação — ainda sem médico atribuído. */
        public ItemFila(String pacienteId, String triagemId, String corDeRisco,
                        LocalDateTime finalizadaEm, String nomePaciente, String tutorId) {
            this(pacienteId, triagemId, corDeRisco, finalizadaEm,
                 nomePaciente, tutorId, null, null);
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
        String nomeMedico
    ) {
        public static ItemFilaDTO de(ItemFila i) {
            return new ItemFilaDTO(
                i.pacienteId(), i.triagemId(), i.corDeRisco(),
                i.finalizadaEm(), i.nomePaciente(), i.tutorId(),
                i.medicoId(), i.nomeMedico());
        }
    }
}