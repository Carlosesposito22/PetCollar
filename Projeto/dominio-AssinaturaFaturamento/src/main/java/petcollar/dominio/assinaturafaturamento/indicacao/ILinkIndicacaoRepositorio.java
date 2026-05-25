package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.List;

public interface ILinkIndicacaoRepositorio {
    void save(LinkIndicacao link);
    LinkIndicacao findById(LinkIndicacaoId id);
    LinkIndicacao findByTutorId(String tutorId);
    LinkIndicacao findByCodigo(String codigo);
    List<LinkIndicacao> findByStatus(StatusLink status);
}
