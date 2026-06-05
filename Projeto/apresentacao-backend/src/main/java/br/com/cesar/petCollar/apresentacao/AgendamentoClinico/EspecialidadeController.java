package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.IEspecialidadeRepositorio;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.EspecialidadeDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.MedicoDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de especialidades e filtro de médicos por especialidade (RN 2).
 */
@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadeController {

    private final IEspecialidadeRepositorio especialidadeRepositorio;

    public EspecialidadeController(IEspecialidadeRepositorio especialidadeRepositorio) {
        this.especialidadeRepositorio = especialidadeRepositorio;
    }

    @GetMapping
    public List<EspecialidadeDTO> listar() {
        return especialidadeRepositorio.listarTodas().stream()
            .map(EspecialidadeDTO::de)
            .toList();
    }

    @GetMapping("/{id}/medicos")
    public List<MedicoDTO> medicos(@PathVariable String id) {
        return especialidadeRepositorio.listarMedicosDaEspecialidade(EspecialidadeId.de(id)).stream()
            .map(MedicoDTO::de)
            .toList();
    }
}
