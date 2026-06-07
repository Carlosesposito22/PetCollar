import { useCallback, useEffect, useRef, useState } from "react";

export type EstadoPolling<T> = {
  dados: T | null;
  carregando: boolean; // primeira carga (ainda sem dados)
  atualizando: boolean; // refetch em andamento (já há dados)
  erro: Error | null;
  ultimaAtualizacao: number | null; // epoch ms da última carga bem-sucedida
  recarregar: () => void;
};

type Opcoes<T> = {
  intervaloMs: number;
  /** Quando false, o polling para (mas a carga inicial e o recarregar manual seguem valendo). */
  ativo?: boolean;
  /** Muda para reiniciar (nova carga) — ex.: o id da rota. */
  chave?: string;
  /** Encerra o polling assim que o dado satisfaz o predicado (ex.: status terminal). */
  pararQuando?: (dados: T) => boolean;
};

/**
 * Polling adaptativo e genérico:
 * - intervalo configurável por tela (tutor 10s, lista 15s, detalhe 5s);
 * - pausa automática quando a aba está em background (`document.hidden`);
 * - para quando `ativo` é false (ex.: protocolo em estado terminal);
 * - `recarregar()` força uma atualização manual.
 *
 * A UI não decide regra de negócio — apenas reflete o que o backend devolve.
 */
export function usePolling<T>(loader: () => Promise<T>, opcoes: Opcoes<T>): EstadoPolling<T> {
  const { intervaloMs, ativo = true, chave, pararQuando } = opcoes;

  const [dados, setDados] = useState<T | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState<Error | null>(null);
  const [ultimaAtualizacao, setUltimaAtualizacao] = useState<number | null>(null);

  // Mantém o loader mais recente sem reiniciar o intervalo a cada render.
  const loaderRef = useRef(loader);
  loaderRef.current = loader;
  const pararQuandoRef = useRef(pararQuando);
  pararQuandoRef.current = pararQuando;
  const temDadosRef = useRef(false);
  const montadoRef = useRef(true);
  const pararRef = useRef(false);

  const buscar = useCallback(async () => {
    if (temDadosRef.current) setAtualizando(true);
    try {
      const resultado = await loaderRef.current();
      if (!montadoRef.current) return;
      setDados(resultado);
      temDadosRef.current = true;
      setErro(null);
      setUltimaAtualizacao(Date.now());
      if (pararQuandoRef.current && pararQuandoRef.current(resultado)) pararRef.current = true;
    } catch (e) {
      if (!montadoRef.current) return;
      setErro(e instanceof Error ? e : new Error(String(e)));
    } finally {
      if (montadoRef.current) {
        setCarregando(false);
        setAtualizando(false);
      }
    }
  }, []);

  // Carga inicial / reinício quando a chave muda.
  useEffect(() => {
    montadoRef.current = true;
    temDadosRef.current = false;
    pararRef.current = false;
    setDados(null);
    setCarregando(true);
    setErro(null);
    void buscar();
    return () => {
      montadoRef.current = false;
    };
  }, [chave, buscar]);

  // Loop de polling com pausa em background e parada em estado terminal.
  useEffect(() => {
    if (!ativo) return;
    let timer: number | undefined;

    const agendar = () => {
      timer = window.setInterval(() => {
        if (pararRef.current) {
          if (timer) window.clearInterval(timer);
          return;
        }
        if (document.visibilityState === "visible") void buscar();
      }, intervaloMs);
    };
    const aoMudarVisibilidade = () => {
      if (!pararRef.current && document.visibilityState === "visible") void buscar();
    };

    agendar();
    document.addEventListener("visibilitychange", aoMudarVisibilidade);
    return () => {
      if (timer) window.clearInterval(timer);
      document.removeEventListener("visibilitychange", aoMudarVisibilidade);
    };
  }, [ativo, intervaloMs, chave, buscar]);

  return { dados, carregando, atualizando, erro, ultimaAtualizacao, recarregar: buscar };
}
