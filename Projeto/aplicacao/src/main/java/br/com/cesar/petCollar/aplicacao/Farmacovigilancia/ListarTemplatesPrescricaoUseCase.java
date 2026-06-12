package br.com.cesar.petCollar.aplicacao.Farmacovigilancia;

import java.util.List;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.ITemplatePrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricao;

public class ListarTemplatesPrescricaoUseCase {

    private final ITemplatePrescricaoRepositorio repositorio;

    public ListarTemplatesPrescricaoUseCase(ITemplatePrescricaoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("ITemplatePrescricaoRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public List<TemplatePrescricao> executar() { return repositorio.listarTodos(); }
}
