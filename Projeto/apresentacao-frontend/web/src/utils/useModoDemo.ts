import { useCallback, useEffect, useState } from "react";

const API_DEMO = "/api/demo";

export function useModoDemo() {
  const [ativo, setAtivo] = useState(false);
  const [carregando, setCarregando] = useState(false);

  // Consulta status atual ao montar
  useEffect(() => {
    fetch(`${API_DEMO}/status`)
      .then((r) => r.json())
      .then((d: { ativo: boolean }) => setAtivo(d.ativo))
      .catch(() => {});
  }, []);

  const alternar = useCallback(async () => {
    if (carregando) return;
    setCarregando(true);
    try {
      const endpoint = ativo ? `${API_DEMO}/desativar` : `${API_DEMO}/ativar`;
      const res = await fetch(endpoint, { method: "POST" });
      if (res.ok) {
        const d: { ativo: boolean } = await res.json();
        setAtivo(d.ativo);
      }
    } finally {
      setCarregando(false);
    }
  }, [ativo, carregando]);

  // Shift+D aciona o toggle globalmente
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.shiftKey && e.key === "PET" && !e.repeat) alternar();
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [alternar]);

  return { ativo, carregando };
}
