import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../../services/protocoloService";
import { BannerErro, Skeleton } from "../../components/compartilhados/Primitivos";
import { ToastProvider, useToast } from "../../components/compartilhados/Toast";
import { FormularioConfiguracao } from "../../components/administrador/FormularioConfiguracao";
import { useConfiguracaoVigente } from "../../hooks/useConfiguracaoVigente";
import { useProtocoloService } from "../../services/useProtocoloService";
import type { RequisicaoConfigurarProtocolo } from "../../tipos";

export function ConfiguracaoProtocoloPage() {
  return (
    <ToastProvider>
      <ConfiguracaoInterna />
    </ToastProvider>
  );
}

function ConfiguracaoInterna() {
  const navigate = useNavigate();
  const toast = useToast();
  const service = useProtocoloService();
  const { dados, carregando, erro, recarregar } = useConfiguracaoVigente();

  const [salvando, setSalvando] = useState(false);
  const [erroSalvar, setErroSalvar] = useState<string | null>(null);

  async function salvar(payload: RequisicaoConfigurarProtocolo) {
    setSalvando(true);
    setErroSalvar(null);
    try {
      const nova = await service.salvarConfiguracao(payload);
      toast.sucesso(`Configuração salva (versão ${nova.versao}).`);
      recarregar();
    } catch (e) {
      setErroSalvar(e instanceof ApiError ? e.message : "Falha ao salvar a configuração.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="mx-auto max-w-6xl space-y-5 p-6">
      <header>
        <h1 className="text-xl font-semibold text-ink-900">Configuração do protocolo</h1>
        <p className="text-sm text-ink-500">Parâmetros de contato e escalonamento (RN 1, 2, 6).</p>
      </header>

      <div className="rounded-2xl border border-brand-200 bg-brand-50 px-5 py-3 text-sm text-brand-800">
        Esta configuração se aplica a todos os <strong>novos</strong> protocolos. Protocolos em
        andamento continuam usando a versão vigente no momento de sua ativação.
      </div>

      {erro && <BannerErro mensagem={erro.message} onTentarNovamente={recarregar} />}

      {carregando ? (
        <Skeleton className="h-96" />
      ) : (
        <FormularioConfiguracao
          key={dados?.versao ?? "nova"}
          inicial={dados}
          salvando={salvando}
          erro={erroSalvar}
          onSalvar={salvar}
          onVerHistorico={() => navigate("/admin/protocolos/configuracao/historico")}
        />
      )}
    </div>
  );
}
