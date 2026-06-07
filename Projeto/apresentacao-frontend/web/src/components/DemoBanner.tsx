type Props = { carregando: boolean };

/**
 * Faixa amarela fixada no topo indicando que o Modo Demonstração está ativo.
 * Desaparece automaticamente quando o modo é desativado (Shift+D).
 */
export function DemoBanner({ carregando }: Props) {
  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        zIndex: 9999,
        background: "#fbbf24",
        color: "#1c1917",
        textAlign: "center",
        padding: "6px 12px",
        fontSize: "13px",
        fontWeight: 600,
        letterSpacing: "0.02em",
      }}
    >
      {carregando
        ? "⏳ Modo Demonstração — aguarde…"
        : "🐾 Modo Demonstração ativo — pressione Ctrl+Shift+D para desativar"}
    </div>
  );
}
