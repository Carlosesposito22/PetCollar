package petCollar.dominio.AtendimentoClinico.relatorio;

import petCollar.dominio.AtendimentoClinico.estrategia.FabricaDeValidadorRelatorio;
import petCollar.dominio.AtendimentoClinico.estrategia.IValidadorRelatorioStrategy;

public class AssinaturaDigitalService {

    private final IRelatorioClinicoRepositorio repositorio;

    public AssinaturaDigitalService(IRelatorioClinicoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("Repositório de relatório clínico não pode ser nulo.");
        this.repositorio = repositorio;
    }

    public RelatorioClinico assinarRelatorio(RelatorioClinicoId relatorioId) {
        if (relatorioId == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        RelatorioClinico relatorio = repositorio.buscarPorId(relatorioId)
            .orElseThrow(() -> new IllegalArgumentException("Relatório clínico não encontrado com o id informado."));

        IValidadorRelatorioStrategy estrategia = FabricaDeValidadorRelatorio.criar(relatorio.getTipoRelatorio());
        estrategia.validarParaAssinatura(relatorio);

        relatorio.assinarDigitalmente();
        repositorio.salvar(relatorio);
        return relatorio;
    }

    public RelatorioClinico atualizarDiagnostico(RelatorioClinicoId relatorioId, String diagnosticoTecnico) {
        if (relatorioId == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        RelatorioClinico relatorio = repositorio.buscarPorId(relatorioId)
            .orElseThrow(() -> new IllegalArgumentException("Relatório clínico não encontrado com o id informado."));
        relatorio.preencherDiagnosticoTecnico(diagnosticoTecnico);
        repositorio.salvar(relatorio);
        return relatorio;
    }

    public RelatorioClinico adicionarAnexo(RelatorioClinicoId relatorioId, AnexoRelatorio anexo) {
        if (relatorioId == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        RelatorioClinico relatorio = repositorio.buscarPorId(relatorioId)
            .orElseThrow(() -> new IllegalArgumentException("Relatório clínico não encontrado com o id informado."));
        relatorio.adicionarAnexo(anexo);
        repositorio.salvar(relatorio);
        return relatorio;
    }
}
