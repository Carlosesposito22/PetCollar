package petCollar.dominio.AtendimentoClinico.relatorio;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petCollar.dominio.AtendimentoClinico.estrategia.FabricaDeValidadorRelatorio;
import petCollar.dominio.AtendimentoClinico.estrategia.IValidadorRelatorioStrategy;

import java.util.List;

/**
 * Serviço de domínio que orquestra o ciclo de vida do relatório clínico evolutivo (F-10).
 *
 * <p>Padrão Strategy: ao assinar o relatório, delega a validação de completude
 * à estratégia selecionada pela {@link FabricaDeValidadorRelatorio} com base no
 * {@link TipoRelatorio}. Relatórios cirúrgicos exigem campos adicionais (RN-124)
 * sem que o serviço precise conhecer cada regra concreta.
 */
public class RelatorioClinicoService {

    private final IRelatorioClinicoRepositorio repositorio;

    public RelatorioClinicoService(IRelatorioClinicoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("Repositório de relatório clínico não pode ser nulo.");
        this.repositorio = repositorio;
    }

    public RelatorioClinico iniciarRelatorio(AtendimentoId atendimentoId, PacienteId pacienteId,
                                              MedicoId medicoId, TipoRelatorio tipoRelatorio) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("AtendimentoId não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        if (medicoId == null)
            throw new IllegalArgumentException("MedicoId não pode ser nulo.");
        if (repositorio.existePorAtendimento(atendimentoId))
            throw new IllegalStateException("Já existe um relatório clínico para este atendimento.");

        TipoRelatorio tipo = tipoRelatorio != null ? tipoRelatorio : TipoRelatorio.ROTINEIRO;
        RelatorioClinico relatorio = new RelatorioClinico(
            RelatorioClinicoId.gerar(), atendimentoId, pacienteId, medicoId, tipo);
        repositorio.salvar(relatorio);
        return relatorio;
    }

    public RelatorioClinico atualizarConteudo(RelatorioClinicoId id, String diagnostico,
                                              String resumoParaTutor, String orientacoesManejo,
                                              String cuidadosPosOp, String tempoRecuperacao) {
        if (id == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        RelatorioClinico relatorio = buscarPorId(id);
        if (diagnostico != null && !diagnostico.isBlank())
            relatorio.preencherDiagnosticoTecnico(diagnostico);
        if (resumoParaTutor != null && !resumoParaTutor.isBlank())
            relatorio.preencherResumoParaTutor(resumoParaTutor);
        if (orientacoesManejo != null && !orientacoesManejo.isBlank())
            relatorio.preencherOrientacoesManejo(orientacoesManejo);
        if (cuidadosPosOp != null && !cuidadosPosOp.isBlank())
            relatorio.preencherCuidadosPosOperatorios(cuidadosPosOp);
        if (tempoRecuperacao != null && !tempoRecuperacao.isBlank())
            relatorio.preencherTempoRecuperacaoEstimado(tempoRecuperacao);
        repositorio.salvar(relatorio);
        return relatorio;
    }

    public RelatorioClinico adicionarMedicamento(RelatorioClinicoId id, String medicamento) {
        if (id == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        RelatorioClinico relatorio = buscarPorId(id);
        relatorio.adicionarMedicamentoPrescrito(medicamento);
        repositorio.salvar(relatorio);
        return relatorio;
    }

    public RelatorioClinico assinarRelatorio(RelatorioClinicoId relatorioId) {
        if (relatorioId == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        RelatorioClinico relatorio = buscarPorId(relatorioId);

        IValidadorRelatorioStrategy estrategia = FabricaDeValidadorRelatorio.criar(relatorio.getTipoRelatorio());
        estrategia.validarParaAssinatura(relatorio);

        relatorio.assinarDigitalmente();
        repositorio.salvar(relatorio);
        return relatorio;
    }

    public RelatorioClinico buscarPorId(RelatorioClinicoId relatorioId) {
        if (relatorioId == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        return repositorio.buscarPorId(relatorioId)
            .orElseThrow(() -> new IllegalArgumentException("Relatório clínico não encontrado."));
    }

    public List<RelatorioClinico> listarPorPaciente(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        return repositorio.listarPorPaciente(pacienteId);
    }

    public List<RelatorioClinico> buscarHistoricoComparativo(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        return repositorio.buscarUltimos3PorPaciente(pacienteId);
    }
}
