package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AutenticacaoService(UsuarioRepositorio repositorio, PasswordEncoder encoder, JwtService jwt) {
        this.repositorio = repositorio;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public RespostaAutenticacao autenticar(RequisicaoLogin requisicao) {
        UsuarioAutenticavel usuario = repositorio.buscar(requisicao.perfil(), requisicao.identificador())
                .orElseThrow(() -> new CredenciaisInvalidasException());

        if (!encoder.matches(requisicao.senha(), usuario.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        if (usuario.status() == StatusConta.SUSPENSA) {
            throw new ContaSuspensaException();
        }

        if (usuario.status() == StatusConta.PENDENTE_PAGAMENTO) {
            throw new PagamentoPendenteException(usuario.identificador());
        }

        JwtService.TokenEmitido emitido = jwt.emitirPara(usuario);
        return new RespostaAutenticacao(
                emitido.token(),
                new RespostaAutenticacao.Usuario(usuario.identificador(), usuario.perfil(), usuario.nome()),
                emitido.expiraEm()
        );
    }
}
