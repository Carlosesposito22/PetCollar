package petcollar.dominio.farmacovigilancia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CronogramaAdministracao {

    private final List<SlotHorario> slots;

    public CronogramaAdministracao(List<SlotHorario> slots) {
        if (slots == null || slots.isEmpty())
            throw new IllegalArgumentException("slots não pode ser vazio.");
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
    }

    public List<SlotHorario> getSlots() { return slots; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CronogramaAdministracao)) return false;
        return Objects.equals(slots, ((CronogramaAdministracao) o).slots);
    }

    @Override
    public int hashCode() { return Objects.hash(slots); }
}
