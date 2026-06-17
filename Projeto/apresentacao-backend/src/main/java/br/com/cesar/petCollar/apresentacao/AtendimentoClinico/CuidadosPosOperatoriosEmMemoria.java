package br.com.cesar.petCollar.apresentacao.AtendimentoClinico;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CuidadosPosOperatoriosEmMemoria {

    private final Map<String, CuidadoPosOp> porPaciente = new ConcurrentHashMap<>();

    public void registrar(String pacienteId, String cuidados, String tempoRecuperacao,
                          int diasCuidado, String medico) {
        LocalDate hoje = LocalDate.now();
        LocalDate validoAte = hoje.plusDays(Math.max(diasCuidado, 0));
        porPaciente.put(pacienteId,
            new CuidadoPosOp(cuidados, tempoRecuperacao, validoAte, medico, hoje));
    }

    public Optional<CuidadoPosOp> buscarAtivo(String pacienteId) {
        CuidadoPosOp cuidado = porPaciente.get(pacienteId);
        if (cuidado == null) return Optional.empty();
        if (cuidado.validoAte().isBefore(LocalDate.now())) {
            porPaciente.remove(pacienteId);
            return Optional.empty();
        }
        return Optional.of(cuidado);
    }

    public void remover(String pacienteId) {
        porPaciente.remove(pacienteId);
    }

    public record CuidadoPosOp(String cuidados, String tempoRecuperacao,
                               LocalDate validoAte, String medico, LocalDate registradoEm) {}
}
