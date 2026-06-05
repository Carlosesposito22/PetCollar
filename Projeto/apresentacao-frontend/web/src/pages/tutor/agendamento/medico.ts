/**
 * O backend expõe o médico apenas pelo seu Id (MedicoDTO { id }). Geramos um rótulo
 * amigável e estável a partir do Id enquanto não há cadastro de nome/CRMV.
 */
export function rotuloMedico(medicoId: string): string {
  const curto = medicoId.replace(/-/g, "").slice(0, 6).toUpperCase();
  return `Dr(a). ${curto}`;
}

export function inicialMedico(medicoId: string): string {
  const c = medicoId.replace(/[^a-zA-Z0-9]/g, "").charAt(0);
  return (c || "M").toUpperCase();
}
