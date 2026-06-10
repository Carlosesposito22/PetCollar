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
        fila.sort(Comparator
            .comparingInt((ItemFila i) -> PRIORIDADE.getOrDefault(i.corDeRisco(), 3))
            .thenComparing(ItemFila::finalizadaEm));
        return listar();
    }

    public void remover(String triagemId) {
        fila.removeIf(i -> i.triagemId().equals(triagemId));
    }

    public List<ItemFilaDTO> listar() {
        return fila.stream().map(ItemFilaDTO::de).toList();
    }

    public record ItemFila(
        String pacienteId,
        String triagemId,
        String corDeRisco,
        LocalDateTime finalizadaEm,
        String nomePaciente,
        String tutorId
    ) {}

    public record ItemFilaDTO(
        String pacienteId,
        String triagemId,
        String corDeRisco,
        LocalDateTime finalizadaEm,
        String nomePaciente,
        String tutorId
    ) {
        public static ItemFilaDTO de(ItemFila i) {
            return new ItemFilaDTO(
                i.pacienteId(), i.triagemId(), i.corDeRisco(),
                i.finalizadaEm(), i.nomePaciente(), i.tutorId());
        }
    }
}