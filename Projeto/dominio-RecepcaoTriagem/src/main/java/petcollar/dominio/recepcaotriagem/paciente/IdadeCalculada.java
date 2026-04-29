package petcollar.dominio.recepcaotriagem.paciente;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public final class IdadeCalculada {

    private final int anos;
    private final int meses;
    private final boolean filhote;
    private final boolean geriatrico;

    public IdadeCalculada(int anos, int meses, boolean filhote, boolean geriatrico) {
        if (anos < 0)
            throw new IllegalArgumentException("Anos de idade não pode ser negativo.");
        if (meses < 0 || meses > 11)
            throw new IllegalArgumentException("Meses de idade deve estar entre 0 e 11.");
        this.anos = anos;
        this.meses = meses;
        this.filhote = filhote;
        this.geriatrico = geriatrico;
    }

    public static IdadeCalculada calcular(LocalDate dataNascimento, Especie especie) {
        if (dataNascimento == null)
            throw new IllegalArgumentException("Data de nascimento não pode ser nula.");
        if (especie == null)
            throw new IllegalArgumentException("Espécie não pode ser nula.");

        LocalDate hoje = LocalDate.now();
        Period periodo = Period.between(dataNascimento, hoje);
        int anos = periodo.getYears();
        int meses = periodo.getMonths();
        boolean filhote = anos < 1;

        boolean geriatrico;
        if (especie == Especie.CANINO) {
            geriatrico = anos >= 7;
        } else if (especie == Especie.FELINO) {
            geriatrico = anos >= 10;
        } else {
            geriatrico = anos >= 10;
        }

        return new IdadeCalculada(anos, meses, filhote, geriatrico);
    }

    public int getAnos()          { return anos; }
    public int getMeses()         { return meses; }
    public boolean isFilhote()    { return filhote; }
    public boolean isGeriatrico() { return geriatrico; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdadeCalculada)) return false;
        IdadeCalculada other = (IdadeCalculada) o;
        return anos == other.anos
            && meses == other.meses
            && filhote == other.filhote
            && geriatrico == other.geriatrico;
    }

    @Override
    public int hashCode() { return Objects.hash(anos, meses, filhote, geriatrico); }
}
