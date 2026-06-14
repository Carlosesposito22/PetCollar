package br.com.cesar.petCollar.apresentacao.PortalTutor;

import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.CuidadosPosOperatoriosEmMemoria;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.CuidadosPosOperatoriosEmMemoria.CuidadoPosOp;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/tutor/pacientes")
public class PacienteController {

    private final PortalTutorRepositorio repositorio;
    private final CicloVacinalService cicloVacinalService;
    private final CuidadosPosOperatoriosEmMemoria cuidadosPosOp;

    public PacienteController(PortalTutorRepositorio repositorio,
                               CicloVacinalService cicloVacinalService,
                               CuidadosPosOperatoriosEmMemoria cuidadosPosOp) {
        this.repositorio        = repositorio;
        this.cicloVacinalService = cicloVacinalService;
        this.cuidadosPosOp      = cuidadosPosOp;
    }

    @GetMapping
    public List<PacienteDTO> listar(Principal principal) {
        return repositorio.listarPacientesDoTutor(principal.getName()).stream()
                .map(p -> PacienteDTO.de(p,
                        cicloVacinalService.possuiVacinaEmAtraso(PacienteId.de(p.id())),
                        cuidadosPosOp.buscarAtivo(p.id()).orElse(null)))
                .toList();
    }

    @PostMapping
    public ResponseEntity<PacienteDTO> criar(@Valid @RequestBody RequisicaoPaciente req, Principal principal) {
        Paciente novo = new Paciente(
                repositorio.novoId(), principal.getName(),
                req.nome(), req.especie(), req.raca(), req.nascimento(),
                req.pesoKg(), req.sexo());
        repositorio.salvarPaciente(novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteDTO.de(novo, false, null));
    }

    @PutMapping("/{id}")
    public PacienteDTO atualizar(@PathVariable String id, @Valid @RequestBody RequisicaoPaciente req, Principal principal) {
        Paciente p = obterDoTutor(id, principal);
        p.atualizar(req.nome(), req.especie(), req.raca(), req.nascimento(),
                req.pesoKg(), req.sexo());
        repositorio.salvarPaciente(p);
        return PacienteDTO.de(p,
                cicloVacinalService.possuiVacinaEmAtraso(PacienteId.de(p.id())),
                cuidadosPosOp.buscarAtivo(p.id()).orElse(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id, Principal principal) {
        obterDoTutor(id, principal);
        cicloVacinalService.removerPorPaciente(PacienteId.de(id));
        cuidadosPosOp.remover(id);
        repositorio.removerPaciente(id);
        return ResponseEntity.noContent().build();
    }

    private Paciente obterDoTutor(String id, Principal principal) {
        Paciente p = repositorio.buscarPaciente(id).orElseThrow(PacienteNaoEncontradoException::new);
        boolean pertence = repositorio.listarPacientesDoTutor(principal.getName())
                .stream().anyMatch(x -> x.id().equalsIgnoreCase(id));
        if (!pertence) throw new PacienteNaoEncontradoException();
        return p;
    }

    public record RequisicaoPaciente(
            @NotBlank String nome,
            @NotBlank String especie,
            @NotBlank String raca,
            @NotNull LocalDate nascimento,
            Double pesoKg,
            String sexo
    ) {}

    public record PacienteDTO(
            String id, String nome, String especie, String raca,
            LocalDate nascimento, int idade, Double pesoKg, String sexo,
            boolean vacinaEmAtraso,
            CuidadoPosOpDTO cuidadoPosOp
    ) {
        static PacienteDTO de(Paciente p, boolean vacinaEmAtraso, CuidadoPosOp cuidado) {
            return new PacienteDTO(p.id(), p.nome(), p.especie(), p.raca(),
                    p.nascimento(), p.idadeEmAnos(), p.pesoKg(), p.sexo(), vacinaEmAtraso,
                    CuidadoPosOpDTO.de(cuidado));
        }
    }

    public record CuidadoPosOpDTO(
            String cuidados, String tempoRecuperacao,
            LocalDate validoAte, String medico, LocalDate registradoEm
    ) {
        static CuidadoPosOpDTO de(CuidadoPosOp c) {
            if (c == null) return null;
            return new CuidadoPosOpDTO(c.cuidados(), c.tempoRecuperacao(),
                    c.validoAte(), c.medico(), c.registradoEm());
        }
    }

    public static class PacienteNaoEncontradoException extends RuntimeException {
        public PacienteNaoEncontradoException() { super("Paciente não encontrado."); }
    }

    @ExceptionHandler(PacienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(PacienteController.PacienteNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "PACIENTE_NAO_ENCONTRADO",
                "mensagem", e.getMessage()
        ));
    }
}
