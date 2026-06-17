package petcollar.dominio.recepcaotriagem.prontuario;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Padrão Iterator (GoF) — F01: Prontuário Unificado.
 *
 * Percorre a lista de pacientes vinculados ao tutor verificando,
 * para cada um, se há diagnóstico infectocontagioso nos últimos
 * 40 dias. Separa a lógica de travessia da lógica de varredura
 * epidemiológica, permitindo que VarreduraEpidemiologicaService
 * use o iterador sem conhecer a estrutura interna da coleção.
 */
public class FilaEpidemiologicaIterator implements Iterator<PacienteId> {

    private final List<PacienteId> pacientes;
    private final IDiagnosticoRepositorio diagnosticoRepositorio;
    private final int janelaDias;

    private int cursor;
    private PacienteId proximoInfectante;
    private boolean buscouProximo;

    public FilaEpidemiologicaIterator(List<PacienteId> pacientes,
                                       IDiagnosticoRepositorio diagnosticoRepositorio,
                                       int janelaDias) {
        if (pacientes == null)
            throw new IllegalArgumentException("Lista de pacientes não pode ser nula.");
        if (diagnosticoRepositorio == null)
            throw new IllegalArgumentException("Repositório de diagnósticos não pode ser nulo.");
        if (janelaDias <= 0)
            throw new IllegalArgumentException("Janela de dias deve ser positiva.");

        this.pacientes              = List.copyOf(pacientes);
        this.diagnosticoRepositorio = diagnosticoRepositorio;
        this.janelaDias             = janelaDias;
        this.cursor                 = 0;
        this.proximoInfectante      = null;
        this.buscouProximo          = false;
    }

    /**
     * Avança internamente até encontrar o próximo paciente com
     * diagnóstico infectocontagioso no período configurado,
     * ou até esgotar a lista.
     */
    @Override
    public boolean hasNext() {
        if (!buscouProximo) {
            proximoInfectante = buscarProximo();
            buscouProximo     = true;
        }
        return proximoInfectante != null;
    }

    @Override
    public PacienteId next() {
        if (!hasNext())
            throw new NoSuchElementException("Não há mais pacientes infectocontagiosos na lista.");
        buscouProximo = false;
        PacienteId resultado = proximoInfectante;
        proximoInfectante    = null;
        return resultado;
    }

    /** Retorna o número de pacientes já percorridos até o momento. */
    public int posicaoAtual() {
        return cursor;
    }

    /** Indica se algum infectante foi encontrado sem consumir o iterador. */
    public boolean possuiInfectante() {
        return hasNext();
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    private PacienteId buscarProximo() {
        while (cursor < pacientes.size()) {
            PacienteId candidato = pacientes.get(cursor);
            cursor++;
            List<String> infectantes =
                diagnosticoRepositorio.findInfectantesUltimos(janelaDias, candidato);
            if (infectantes != null && !infectantes.isEmpty()) {
                return candidato;
            }
        }
        return null;
    }
}