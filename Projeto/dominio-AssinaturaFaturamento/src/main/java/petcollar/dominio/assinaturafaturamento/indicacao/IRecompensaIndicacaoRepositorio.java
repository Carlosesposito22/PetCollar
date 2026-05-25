package petcollar.dominio.assinaturafaturamento.indicacao;

public interface IRecompensaIndicacaoRepositorio {
    void save(RecompensaIndicacao recompensa);
    RecompensaIndicacao findById(RecompensaIndicacaoId id);
    RecompensaIndicacao findByConversaoId(ConversaoId conversaoId);
}
