package petcollar.dominio.farmacovigilancia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Prescricao {

    private final PrescricaoId id;
    private final List<ItemPrescricao> itens;
    private StatusPrescricao status;
    private CronogramaAdministracao cronograma;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtor de CRIAÇÃO
    public Prescricao(PrescricaoId id) {
        if (id == null)
            throw new IllegalArgumentException("id não pode ser nulo.");
        this.id = id;
        this.itens = new ArrayList<>();
        this.status = StatusPrescricao.RASCUNHO;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public Prescricao(PrescricaoId id,
                      List<ItemPrescricao> itens,
                      StatusPrescricao status,
                      CronogramaAdministracao cronograma,
                      LocalDateTime criadoEm,
                      LocalDateTime atualizadoEm) {
        this.id = id;
        this.itens = new ArrayList<>(itens != null ? itens : Collections.emptyList());
        this.status = status;
        this.cronograma = cronograma;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void adicionarItem(ItemPrescricao item) {
        if (this.status == StatusPrescricao.EMITIDA)
            throw new IllegalStateException("Prescrição EMITIDA é imutável.");
        if (item == null)
            throw new IllegalArgumentException("item não pode ser nulo.");
        this.itens.add(item);
        this.atualizadoEm = LocalDateTime.now();
    }

    public void travarPorInteracao() {
        if (this.status == StatusPrescricao.EMITIDA)
            throw new IllegalStateException("Prescrição EMITIDA é imutável.");
        this.status = StatusPrescricao.TRAVADA_AGUARDANDO_REVISAO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void emitir() {
        if (this.status == StatusPrescricao.EMITIDA)
            throw new IllegalStateException("Prescrição já foi emitida.");
        for (ItemPrescricao item : itens) {
            if (item.getStatusDosagem() == StatusDosagem.TRAVADO_POR_DOSE
                    || item.getStatusDosagem() == StatusDosagem.TRAVADO_POR_INTERACAO) {
                throw new IllegalStateException(
                    "Não é possível emitir uma prescrição com itens travados.");
            }
        }
        this.status = StatusPrescricao.EMITIDA;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void definirCronograma(CronogramaAdministracao cronograma) {
        if (this.status == StatusPrescricao.EMITIDA)
            throw new IllegalStateException("Prescrição EMITIDA é imutável.");
        if (cronograma == null)
            throw new IllegalArgumentException("cronograma não pode ser nulo.");
        this.cronograma = cronograma;
        this.atualizadoEm = LocalDateTime.now();
    }

    public PrescricaoId getId()                         { return id; }
    public List<ItemPrescricao> getItens()              { return Collections.unmodifiableList(itens); }
    public StatusPrescricao getStatus()                 { return status; }
    public CronogramaAdministracao getCronograma()      { return cronograma; }
    public LocalDateTime getCriadoEm()                  { return criadoEm; }
    public LocalDateTime getAtualizadoEm()              { return atualizadoEm; }
}
