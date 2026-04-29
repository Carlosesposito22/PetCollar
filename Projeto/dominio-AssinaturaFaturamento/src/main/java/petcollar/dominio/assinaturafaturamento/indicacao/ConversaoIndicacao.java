package petcollar.dominio.assinaturafaturamento.indicacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversaoIndicacao {

    private final ConversaoId id;
    private LinkIndicacaoId linkIndicacaoId;
    private String tutorIndicadorId;
    private final String cpfIndicado;
    private AssinaturaMetodoPagamento metodoPagamento;
    private StatusConversao status;
    private final List<EventoRastreio> trilhaAuditoria;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public ConversaoIndicacao(ConversaoId id, LinkIndicacaoId linkIndicacaoId,
                              String tutorIndicadorId, String cpfIndicado) {
        if (id == null)
            throw new IllegalArgumentException("ConversaoId não pode ser nulo.");
        if (linkIndicacaoId == null)
            throw new IllegalArgumentException("LinkIndicacaoId não pode ser nulo.");
        if (tutorIndicadorId == null || tutorIndicadorId.isBlank())
            throw new IllegalArgumentException("tutorIndicadorId não pode ser vazio.");
        if (cpfIndicado == null || cpfIndicado.isBlank())
            throw new IllegalArgumentException("cpfIndicado não pode ser vazio.");
        this.id = id;
        this.linkIndicacaoId = linkIndicacaoId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.cpfIndicado = cpfIndicado;
        this.status = StatusConversao.AGUARDANDO_CADASTRO;
        this.trilhaAuditoria = new ArrayList<>();
        this.criadoEm = LocalDateTime.now();
    }

    public ConversaoIndicacao(ConversaoId id, LinkIndicacaoId linkIndicacaoId,
                              String tutorIndicadorId, String cpfIndicado,
                              AssinaturaMetodoPagamento metodoPagamento,
                              StatusConversao status, List<EventoRastreio> trilhaAuditoria,
                              LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.linkIndicacaoId = linkIndicacaoId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.cpfIndicado = cpfIndicado;
        this.metodoPagamento = metodoPagamento;
        this.status = status;
        this.trilhaAuditoria = new ArrayList<>(trilhaAuditoria != null ? trilhaAuditoria : List.of());
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void confirmarCadastro() {
        if (this.status != StatusConversao.AGUARDANDO_CADASTRO)
            throw new IllegalStateException(
                    "Só é possível confirmar cadastro quando o status for AGUARDANDO_CADASTRO.");
        this.status = StatusConversao.AGUARDANDO_PAGAMENTO;
        this.trilhaAuditoria.add(new EventoRastreio(TipoEventoRastreio.CADASTRO_INICIADO, "{}"));
        this.atualizadoEm = LocalDateTime.now();
    }

    public void registrarMetodoPagamento(AssinaturaMetodoPagamento metodo) {
        if (metodo == null)
            throw new IllegalArgumentException("Método de pagamento não pode ser nulo.");
        this.metodoPagamento = metodo;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void confirmar() {
        if (this.status != StatusConversao.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException(
                    "Só é possível confirmar conversão quando o status for AGUARDANDO_PAGAMENTO.");
        this.status = StatusConversao.CONFIRMADA;
        this.trilhaAuditoria.add(new EventoRastreio(TipoEventoRastreio.PAGAMENTO_CONFIRMADO, "{}"));
        this.atualizadoEm = LocalDateTime.now();
    }

    public void invalidarFraude() {
        this.status = StatusConversao.INVALIDA_FRAUDE;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void invalidarCpfDuplicado() {
        this.status = StatusConversao.INVALIDA_CPF_DUPLICADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void adicionarEvento(EventoRastreio evento) {
        if (evento == null)
            throw new IllegalArgumentException("EventoRastreio não pode ser nulo.");
        this.trilhaAuditoria.add(evento);
        this.atualizadoEm = LocalDateTime.now();
    }

    public void atribuirIndicador(String novoTutorIndicadorId, LinkIndicacaoId novoLinkId) {
        if (novoTutorIndicadorId == null || novoTutorIndicadorId.isBlank())
            throw new IllegalArgumentException("Novo tutorIndicadorId não pode ser vazio.");
        if (novoLinkId == null)
            throw new IllegalArgumentException("Novo LinkIndicacaoId não pode ser nulo.");
        this.tutorIndicadorId = novoTutorIndicadorId;
        this.linkIndicacaoId = novoLinkId;
        this.atualizadoEm = LocalDateTime.now();
    }

    public ConversaoId getId()                          { return id; }
    public LinkIndicacaoId getLinkIndicacaoId()         { return linkIndicacaoId; }
    public String getTutorIndicadorId()                 { return tutorIndicadorId; }
    public String getCpfIndicado()                      { return cpfIndicado; }
    public AssinaturaMetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public StatusConversao getStatus()                  { return status; }
    public List<EventoRastreio> getTrilhaAuditoria()    { return Collections.unmodifiableList(trilhaAuditoria); }
    public LocalDateTime getCriadoEm()                  { return criadoEm; }
    public LocalDateTime getAtualizadoEm()              { return atualizadoEm; }
}
