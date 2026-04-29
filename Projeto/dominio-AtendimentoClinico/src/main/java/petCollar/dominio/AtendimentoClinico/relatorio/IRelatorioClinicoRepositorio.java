package petCollar.dominio.AtendimentoClinico.relatorio;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public interface IRelatorioClinicoRepositorio {
    void save(RelatorioClinico relatorio);
    RelatorioClinico findById(RelatorioClinicoId id);
    List<RelatorioClinico> findByAtendimentoId(AtendimentoId atendimentoId);
    List<RelatorioClinico> findByPacienteId(PacienteId pacienteId);
    boolean existsByAtendimentoId(AtendimentoId atendimentoId);
}
