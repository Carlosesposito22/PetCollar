package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.IEspecialidadeRepositorio;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.EspecialidadeDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.MedicoDTO;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadeController {

    private final IEspecialidadeRepositorio especialidadeRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public EspecialidadeController(IEspecialidadeRepositorio especialidadeRepositorio,
                                   UsuarioRepositorio usuarioRepositorio) {
        this.especialidadeRepositorio = especialidadeRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
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
            .map(m -> MedicoDTO.de(m, resolverNomeMedico(m)))
            .toList();
    }

    private String resolverNomeMedico(MedicoId medicoId) {
        return usuarioRepositorio.buscar(Perfil.MEDICO_VETERINARIO, medicoId.getValor())
            .map(u -> u.nome())
            .orElse(medicoId.getValor());
    }
}
