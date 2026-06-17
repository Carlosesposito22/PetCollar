import { useCallback, useEffect, useState } from "react";
import { useProtocoloService } from "../services/useProtocoloService";
import type { ResumoAtendimentoDTO } from "../tipos";

/** Carrega uma vez a lista de atendimentos em andamento (sem polling — é síncrono ao abrir o modal). */
export function useAtendimentosEmAndamento(ativo: boolean) {
  const service = useProtocoloService();
  const [dados, setDados] = useState<ResumoAtendimentoDTO[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const lista = await service.listarAtendimentosEmAndamento();
      setDados(lista);
    } catch {
      setErro("Não foi possível carregar os atendimentos em andamento.");
    } finally {
      setCarregando(false);
    }
  }, [service]);

  useEffect(() => {
    if (ativo) carregar();
  }, [ativo, carregar]);

  return { dados, carregando, erro, recarregar: carregar };
}
