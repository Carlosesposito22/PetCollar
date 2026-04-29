package petcollar.dominio.farmacovigilancia;

import java.time.LocalDateTime;

public class MatrizInteracao {

    private final MatrizInteracaoId id;
    private final RegraInteracao regra;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO
    public MatrizInteracao(MatrizInteracaoId id, RegraInteracao regra) {
        if (id == null)
            throw new IllegalArgumentException("id não pode ser nulo.");
        if (regra == null)
            throw new IllegalArgumentException("regra não pode ser nula.");
        this.id = id;
        this.regra = regra;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public MatrizInteracao(MatrizInteracaoId id, RegraInteracao regra, LocalDateTime criadoEm) {
        this.id = id;
        this.regra = regra;
        this.criadoEm = criadoEm;
    }

    public MatrizInteracaoId getId()   { return id; }
    public RegraInteracao getRegra()   { return regra; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
