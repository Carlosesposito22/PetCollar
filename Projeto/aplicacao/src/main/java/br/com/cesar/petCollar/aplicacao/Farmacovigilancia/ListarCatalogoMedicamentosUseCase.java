package br.com.cesar.petCollar.aplicacao.Farmacovigilancia;

import java.util.List;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.IMedicamentoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;

public class ListarCatalogoMedicamentosUseCase {

    private final IMedicamentoRepositorio repositorio;

    public ListarCatalogoMedicamentosUseCase(IMedicamentoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IMedicamentoRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public List<Medicamento> executar() { return repositorio.listarTodos(); }
}
