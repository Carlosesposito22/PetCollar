package petCollar.dominio.AtendimentoClinico.relatorio;

public class AssinaturaDigitalService {

    private final IRelatorioClinicoRepositorio repositorio;

    public AssinaturaDigitalService(IRelatorioClinicoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Assina digitalmente o relatório, tornando-o imutável (RN-118).
     * Exige que diagnóstico técnico e orientações de manejo estejam preenchidos (RN-120).
     */
    public RelatorioClinico assinarRelatorio(RelatorioClinicoId relatorioId) {
        RelatorioClinico relatorio = repositorio.findById(relatorioId);
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório clínico não encontrado com o id informado.");

        relatorio.assinarDigitalmente();
        repositorio.save(relatorio);
        return relatorio;
    }

    /**
     * Tenta modificar o diagnóstico técnico após a assinatura — deve lançar IllegalStateException (RN-118).
     */
    public RelatorioClinico atualizarDiagnostico(RelatorioClinicoId relatorioId,
                                                   String diagnosticoTecnico) {
        RelatorioClinico relatorio = repositorio.findById(relatorioId);
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório clínico não encontrado com o id informado.");

        relatorio.preencherDiagnosticoTecnico(diagnosticoTecnico);
        repositorio.save(relatorio);
        return relatorio;
    }

    /**
     * Adiciona um anexo ao relatório (RN-119).
     */
    public RelatorioClinico adicionarAnexo(RelatorioClinicoId relatorioId, AnexoRelatorio anexo) {
        RelatorioClinico relatorio = repositorio.findById(relatorioId);
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório clínico não encontrado com o id informado.");

        relatorio.adicionarAnexo(anexo);
        repositorio.save(relatorio);
        return relatorio;
    }
}
