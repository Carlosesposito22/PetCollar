package br.com.cesar.petCollar.dominio.Farmacovigilancia.template;

import java.util.List;
import java.util.Optional;

public interface ITemplatePrescricaoRepositorio {

    void salvar(TemplatePrescricao template);

    Optional<TemplatePrescricao> buscarPorId(TemplatePrescricaoId id);

    List<TemplatePrescricao> listarTodos();

    long contar();
}
