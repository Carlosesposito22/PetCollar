package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.List;

public interface IConversaoIndicacaoRepositorio {
    void save(ConversaoIndicacao conversao);
    ConversaoIndicacao findById(ConversaoId id);
    boolean existsByCpfIndicadoEStatusConfirmada(String cpfIndicado);
    boolean existsByHashMetodoForTutorIndicador(String hashMetodo, String tutorIndicadorId);
    List<ConversaoIndicacao> findByLinkIndicacaoId(LinkIndicacaoId linkId);
    List<ConversaoIndicacao> findByStatus(StatusConversao status);
}
