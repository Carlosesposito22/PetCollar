package petcollar.dominio.farmacovigilancia;

/**
 * RN-145, RN-146
 * Orquestra a emissão da prescrição, validando se não há itens travados.
 */
public class EmissaoPrescricaoService {

    private final IPrescricaoRepositorio prescricaoRepositorio;

    public EmissaoPrescricaoService(IPrescricaoRepositorio prescricaoRepositorio) {
        this.prescricaoRepositorio = prescricaoRepositorio;
    }

    public Prescricao emitir(PrescricaoId id) {
        Prescricao prescricao = prescricaoRepositorio.findById(id);
        if (prescricao == null)
            throw new IllegalArgumentException("Prescrição não encontrada com o id informado.");
        prescricao.emitir();
        prescricaoRepositorio.save(prescricao);
        return prescricao;
    }
}
