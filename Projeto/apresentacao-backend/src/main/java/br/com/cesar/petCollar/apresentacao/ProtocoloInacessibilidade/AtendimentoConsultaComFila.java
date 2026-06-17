package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.ConsultaJpa;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.ConsultaJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.FilaAtendimentoEmMemoria;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.FilaAtendimentoEmMemoria.ItemFilaDTO;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter composto da porta {@link IConsultaAtendimento}: combina duas fontes
 * de "atendimento em andamento" —
 * <ol>
 *   <li><b>Fila de espera</b> ({@link FilaAtendimentoEmMemoria}): pacientes que
 *       concluíram a triagem e aguardam chamada. Usa o {@code triagemId} como
 *       {@code atendimentoId}, pois ainda não existe consulta clínica associada.</li>
 *   <li><b>Consultas agendadas/confirmadas</b>: atendimentos com status
 *       {@code AGENDADA} ou {@code CONFIRMADA} na agenda formal.</li>
 * </ol>
 * Anotado com {@code @Primary} para sobrepor o {@code AtendimentoConsultaJpa}
 * da infraestrutura, que só conhece a segunda fonte.
 */
@Primary
@Component
public class AtendimentoConsultaComFila implements IConsultaAtendimento {

    private final FilaAtendimentoEmMemoria fila;
    private final ConsultaJpaRepository consultaJpa;

    public AtendimentoConsultaComFila(FilaAtendimentoEmMemoria fila,
                                      ConsultaJpaRepository consultaJpa) {
        this.fila = fila;
        this.consultaJpa = consultaJpa;
    }

    @Override
    public Optional<ResumoAtendimento> buscarResumo(AtendimentoId atendimentoId) {
        // Primeiro tenta encontrar na fila pelo triagemId.
        Optional<ResumoAtendimento> daFila = fila.listar().stream()
            .filter(i -> i.triagemId().equals(atendimentoId.getValor()))
            .map(this::toResumo)
            .findFirst();
        if (daFila.isPresent()) return daFila;

        // Fallback: busca na tabela de consultas pelo id.
        return consultaJpa.findById(atendimentoId.getValor())
            .map(ConsultaJpa::toResumoAtendimento);
    }

    @Override
    public List<ResumoAtendimento> listarEmAndamento() {
        List<ResumoAtendimento> resultado = new ArrayList<>();

        // Pacientes na fila de espera — todos estão ativamente presentes na clínica.
        fila.listar().stream()
            .map(this::toResumo)
            .forEach(resultado::add);

        // Consultas agendadas ou confirmadas (agenda formal).
        consultaJpa.findByStatusIn(List.of("AGENDADA", "CONFIRMADA")).stream()
            .map(ConsultaJpa::toResumoAtendimento)
            .forEach(resultado::add);

        return resultado;
    }

    private ResumoAtendimento toResumo(ItemFilaDTO i) {
        return new ResumoAtendimento(
            AtendimentoId.de(i.triagemId()),
            PacienteId.de(i.pacienteId()),
            TutorId.de(i.tutorId()),
            i.finalizadaEm(),
            true,
            i.nomePaciente());
    }
}
