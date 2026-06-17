package br.com.cesar.petCollar.apresentacao.Farmacovigilancia;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ConsultarPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ListarCatalogoMedicamentosUseCase;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.HistoricoDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.PrescricaoDTO;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.PrescricaoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

@RestController
@RequestMapping("/api/tutor/farmacovigilancia")
public class FarmacovigilanciaDoTutorController {

    private final ConsultarPrescricaoUseCase consultar;
    private final ListarCatalogoMedicamentosUseCase listarCatalogo;

    public FarmacovigilanciaDoTutorController(ConsultarPrescricaoUseCase consultar,
                                              ListarCatalogoMedicamentosUseCase listarCatalogo) {
        this.consultar = consultar;
        this.listarCatalogo = listarCatalogo;
    }

    @GetMapping
    public HistoricoDTO meusTratamentos(Principal principal) {
        List<Prescricao> ativas = consultar.listarAtivasDoTutor(TutorId.de(principal.getName()));
        Map<String, String> nomes = new HashMap<>();
        for (Medicamento m : listarCatalogo.executar()) nomes.put(m.getId().getValor(), m.getNome());
        return new HistoricoDTO(ativas.stream().map(PrescricaoDTO::de).toList(), nomes);
    }

    @GetMapping("/{prescricaoId}")
    public ResponseEntity<PrescricaoDTO> detalhe(@PathVariable String prescricaoId, Principal principal) {
        TutorId tutorAutenticado = TutorId.de(principal.getName());
        return consultar.buscarPorId(PrescricaoId.de(prescricaoId))
                .filter((Prescricao p) -> p.getTutorId().equals(tutorAutenticado))
                .map(PrescricaoDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
