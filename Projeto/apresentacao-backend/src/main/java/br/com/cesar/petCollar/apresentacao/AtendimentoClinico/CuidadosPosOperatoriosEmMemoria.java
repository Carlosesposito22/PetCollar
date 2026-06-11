package br.com.cesar.petCollar.apresentacao.AtendimentoClinico;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena, por paciente, os cuidados pós-operatórios ativos registrados pelo
 * médico ao assinar um relatório cirúrgico (F-10). Serve de ponte para o portal
 * do tutor exibir o alerta de cuidados enquanto a recuperação está em curso.
 *
 * <p>Stand-in em memória (CLAUDE.md §6) enquanto a persistência JPA está
 * desligada. O cuidado expira automaticamente: ao ser consultado após a data de
 * validade, é descartado — fazendo o ícone/alerta sumirem do app do tutor.
 */
@Component
public class CuidadosPosOperatoriosEmMemoria {

    private final Map<String, CuidadoPosOp> porPaciente = new ConcurrentHashMap<>();

    /**
     * Registra o cuidado pós-operatório do paciente, com validade calculada a
     * partir da quantidade de dias de recuperação informada pelo médico.
     */
    public void registrar(String pacienteId, String cuidados, String tempoRecuperacao,
                          int diasCuidado, String medico) {
        LocalDate hoje = LocalDate.now();
        LocalDate validoAte = hoje.plusDays(Math.max(diasCuidado, 0));
        porPaciente.put(pacienteId,
            new CuidadoPosOp(cuidados, tempoRecuperacao, validoAte, medico, hoje));
    }

    /** Cuidado ativo (não expirado) do paciente, se houver. Descarta os expirados. */
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
