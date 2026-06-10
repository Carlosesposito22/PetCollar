import { useEffect, useImperativeHandle, useRef, useState, forwardRef } from "react";

export type SignaturePadHandle = {
  /** Retorna a assinatura como PNG base64, ou "" se estiver em branco. */
  toDataURL: () => string;
  limpar: () => void;
  estaVazio: () => boolean;
};

/**
 * Bloco de assinatura digital: o médico desenha com o mouse ou o dedo (touch).
 * Usado na tela de Relatório Clínico antes de assinar e publicar (F-10 / RN-120).
 */
export const SignaturePad = forwardRef<SignaturePadHandle, { onChange?: (vazio: boolean) => void }>(
  function SignaturePad({ onChange }, ref) {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const desenhando = useRef(false);
    const vazioRef = useRef(true);
    const [vazio, setVazio] = useState(true);

    function marcarVazio(v: boolean) {
      vazioRef.current = v;
      setVazio(v);
      onChange?.(v);
    }

    useEffect(() => {
      const canvas = canvasRef.current;
      if (!canvas) return;
      // Ajusta a resolução interna ao tamanho renderizado (nitidez em telas retina).
      const ratio = window.devicePixelRatio || 1;
      const rect = canvas.getBoundingClientRect();
      canvas.width = rect.width * ratio;
      canvas.height = rect.height * ratio;
      const ctx = canvas.getContext("2d");
      if (!ctx) return;
      ctx.scale(ratio, ratio);
      ctx.lineWidth = 2.2;
      ctx.lineCap = "round";
      ctx.lineJoin = "round";
      ctx.strokeStyle = "#0f172a";
    }, []);

    function pos(e: React.PointerEvent<HTMLCanvasElement>) {
      const rect = canvasRef.current!.getBoundingClientRect();
      return { x: e.clientX - rect.left, y: e.clientY - rect.top };
    }

    function iniciar(e: React.PointerEvent<HTMLCanvasElement>) {
      const ctx = canvasRef.current?.getContext("2d");
      if (!ctx) return;
      desenhando.current = true;
      canvasRef.current!.setPointerCapture(e.pointerId);
      const { x, y } = pos(e);
      ctx.beginPath();
      ctx.moveTo(x, y);
      if (vazioRef.current) marcarVazio(false);
    }

    function mover(e: React.PointerEvent<HTMLCanvasElement>) {
      if (!desenhando.current) return;
      const ctx = canvasRef.current?.getContext("2d");
      if (!ctx) return;
      const { x, y } = pos(e);
      ctx.lineTo(x, y);
      ctx.stroke();
    }

    function finalizar() {
      desenhando.current = false;
    }

    function limpar() {
      const canvas = canvasRef.current;
      const ctx = canvas?.getContext("2d");
      if (!canvas || !ctx) return;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      marcarVazio(true);
    }

    useImperativeHandle(ref, () => ({
      toDataURL: () => (vazioRef.current ? "" : canvasRef.current!.toDataURL("image/png")),
      limpar,
      estaVazio: () => vazioRef.current,
    }));

    return (
      <div>
        <div className="relative">
          <canvas
            ref={canvasRef}
            onPointerDown={iniciar}
            onPointerMove={mover}
            onPointerUp={finalizar}
            onPointerLeave={finalizar}
            className="h-40 w-full touch-none rounded-xl border-2 border-dashed border-ink-300 bg-ink-50/40 cursor-crosshair"
          />
          {vazio && (
            <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center text-center">
              <span className="text-2xl">✍️</span>
              <span className="mt-1 text-sm text-ink-400">Assine aqui com o mouse</span>
            </div>
          )}
        </div>
        <div className="mt-2 flex items-center justify-between">
          <p className="text-xs text-ink-500">
            {vazio ? "A assinatura é obrigatória para publicar." : "Assinatura registrada ✓"}
          </p>
          <button
            type="button"
            onClick={limpar}
            className="rounded-lg border border-ink-300 px-3 py-1 text-xs font-medium text-ink-600 transition hover:bg-ink-50"
          >
            Limpar
          </button>
        </div>
      </div>
    );
  }
);
