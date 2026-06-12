package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

public interface IPlanoRepositorio {

    void salvar(Plano plano);

    Optional<Plano> buscarPorId(PlanoId id);

    /** Plano padrão usado em demos/contratações simples (ex.: Básico Mensal). */
    Optional<Plano> buscarPorNome(String nome);

    List<Plano> listar();

    void remover(PlanoId id);
}
