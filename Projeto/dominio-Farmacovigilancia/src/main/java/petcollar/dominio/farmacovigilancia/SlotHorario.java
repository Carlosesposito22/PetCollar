package petcollar.dominio.farmacovigilancia;

import java.util.Objects;

public final class SlotHorario {

    private final String horario;
    private final ItemPrescricaoId itemId;
    private final double volumeAdministrar;
    private final String notaCuidado;

    public SlotHorario(String horario,
                       ItemPrescricaoId itemId,
                       double volumeAdministrar,
                       String notaCuidado) {
        if (horario == null || horario.isBlank())
            throw new IllegalArgumentException("horario não pode ser vazio.");
        if (itemId == null)
            throw new IllegalArgumentException("itemId não pode ser nulo.");
        if (volumeAdministrar < 0)
            throw new IllegalArgumentException("volumeAdministrar não pode ser negativo.");
        this.horario = horario;
        this.itemId = itemId;
        this.volumeAdministrar = volumeAdministrar;
        this.notaCuidado = notaCuidado;
    }

    public String getHorario()                { return horario; }
    public ItemPrescricaoId getItemId()        { return itemId; }
    public double getVolumeAdministrar()       { return volumeAdministrar; }
    public String getNotaCuidado()             { return notaCuidado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlotHorario)) return false;
        SlotHorario other = (SlotHorario) o;
        return Double.compare(other.volumeAdministrar, volumeAdministrar) == 0
            && Objects.equals(horario, other.horario)
            && Objects.equals(itemId, other.itemId)
            && Objects.equals(notaCuidado, other.notaCuidado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horario, itemId, volumeAdministrar, notaCuidado);
    }
}
