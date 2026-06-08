package petCollar.dominio.AtendimentoClinico.relatorio;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;
import java.util.Optional;

public interface IRelatorioClinicoRepositorio {
    void salvar(RelatorioClinico relatorio);
    Optional<RelatorioClinico> buscarPorId(RelatorioClinicoId id);
    List<RelatorioClinico> listarPorPaciente(PacienteId pacienteId);
    List<RelatorioClinico> listarPorAtendimento(AtendimentoId atendimentoId);
    boolean existePorAtendimento(AtendimentoId atendimentoId);
    List<RelatorioClinico> buscarUltimos3PorPaciente(PacienteId pacienteId);
}
