package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

/**
 * Finaliza o plano nutricional, capturando a assinatura digital (PNG base64
 * vindo do {@code SignaturePad}) e tornando o agregado imutável (RN 8).
 */
public class FinalizarPlanoNutricionalUseCase {

    private final IPlanoNutricionalRepositorio repositorio;

    public FinalizarPlanoNutricionalUseCase(IPlanoNutricionalRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IPlanoNutricionalRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public PlanoNutricional executar(PlanoNutricionalId planoId, MedicoId medicoQueAssina,
                                     String imagemAssinaturaBase64) {
        if (planoId == null)
            throw new IllegalArgumentException("PlanoNutricionalId é obrigatório.");
        PlanoNutricional plano = repositorio.buscarPorId(planoId)
                .orElseThrow(() -> new IllegalArgumentException("Plano nutricional não encontrado: " + planoId));
        plano.finalizar(medicoQueAssina, imagemAssinaturaBase64);
        repositorio.salvar(plano);
        return plano;
    }
}
