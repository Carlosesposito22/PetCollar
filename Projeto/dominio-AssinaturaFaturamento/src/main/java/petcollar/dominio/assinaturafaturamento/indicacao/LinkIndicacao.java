package petcollar.dominio.assinaturafaturamento.indicacao;

import java.time.LocalDateTime;

public class LinkIndicacao {

    private final LinkIndicacaoId id;
    private final String tutorId;
    private final String cpfTutor;
    private final CodigoIndicacao codigoIndicacao;
    private final UrlCompartilhamento urlCompartilhamento;
    private StatusLink status;
    private int totalCliques;
    private int totalConversoes;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public LinkIndicacao(LinkIndicacaoId id, String tutorId, String cpfTutor,
                         CodigoIndicacao codigoIndicacao, UrlCompartilhamento urlCompartilhamento) {
        if (id == null)
            throw new IllegalArgumentException("LinkIndicacaoId não pode ser nulo.");
        if (tutorId == null || tutorId.isBlank())
            throw new IllegalArgumentException("tutorId não pode ser vazio.");
        if (cpfTutor == null || cpfTutor.isBlank())
            throw new IllegalArgumentException("cpfTutor não pode ser vazio.");
        if (codigoIndicacao == null)
            throw new IllegalArgumentException("CodigoIndicacao não pode ser nulo.");
        if (urlCompartilhamento == null)
            throw new IllegalArgumentException("UrlCompartilhamento não pode ser nula.");
        this.id = id;
        this.tutorId = tutorId;
        this.cpfTutor = cpfTutor;
        this.codigoIndicacao = codigoIndicacao;
        this.urlCompartilhamento = urlCompartilhamento;
        this.status = StatusLink.ATIVO;
        this.totalCliques = 0;
        this.totalConversoes = 0;
        this.criadoEm = LocalDateTime.now();
    }

    public LinkIndicacao(LinkIndicacaoId id, String tutorId, String cpfTutor,
                         CodigoIndicacao codigoIndicacao, UrlCompartilhamento urlCompartilhamento,
                         StatusLink status, int totalCliques, int totalConversoes,
                         LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.tutorId = tutorId;
        this.cpfTutor = cpfTutor;
        this.codigoIndicacao = codigoIndicacao;
        this.urlCompartilhamento = urlCompartilhamento;
        this.status = status;
        this.totalCliques = totalCliques;
        this.totalConversoes = totalConversoes;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void registrarClique() {
        if (this.status != StatusLink.ATIVO)
            throw new IllegalStateException("Não é possível registrar clique em link que não está ATIVO.");
        this.totalCliques++;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void registrarConversao() {
        if (this.status != StatusLink.ATIVO)
            throw new IllegalStateException("Não é possível registrar conversão em link que não está ATIVO.");
        this.totalConversoes++;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void suspender() {
        if (this.status == StatusLink.INATIVO)
            throw new IllegalStateException("Não é possível suspender link INATIVO.");
        this.status = StatusLink.SUSPENSO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void inativar() {
        this.status = StatusLink.INATIVO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public LinkIndicacaoId getId()                      { return id; }
    public String getTutorId()                          { return tutorId; }
    public String getCpfTutor()                         { return cpfTutor; }
    public CodigoIndicacao getCodigoIndicacao()         { return codigoIndicacao; }
    public UrlCompartilhamento getUrlCompartilhamento() { return urlCompartilhamento; }
    public StatusLink getStatus()                       { return status; }
    public int getTotalCliques()                        { return totalCliques; }
    public int getTotalConversoes()                     { return totalConversoes; }
    public LocalDateTime getCriadoEm()                  { return criadoEm; }
    public LocalDateTime getAtualizadoEm()              { return atualizadoEm; }
}
