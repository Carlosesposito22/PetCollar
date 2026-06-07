# language: pt
Funcionalidade: Escalonamento progressivo do protocolo
  Como clínica, quero escalonar progressivamente por níveis administrativos e
  clínicos (RN 6) quando ninguém responde, registrando cada avanço de forma
  auditável (RN 7) e notificando o tutor a cada mudança de nível (RN 13). A etapa é
  a subclasse EtapaEscalonamentoService do Template Method: cada execução avança um
  nível e mantém o protocolo em escalonamento até esgotar os níveis.

  Cenário: O escalonamento avança nível a nível
    Dado uma configuração de protocolo vigente
    E um protocolo pronto para escalonamento
    Quando o sistema inicia o escalonamento
    Então o nível de escalonamento atual deve ser "NIVEL_1_ADMINISTRATIVO"
    Quando o sistema avança o nível de escalonamento
    Então o nível de escalonamento atual deve ser "NIVEL_2_COORDENACAO"

  Cenário: Cada escalonamento gera um evento auditável e mantém o estado EM_ESCALONAMENTO
    Dado uma configuração de protocolo vigente
    E um protocolo pronto para escalonamento
    Quando o sistema inicia o escalonamento
    Então deve haver 1 evento de escalonamento registrado
    E o protocolo deve ficar com status "EM_ESCALONAMENTO"

  Cenário: O tutor é notificado a cada mudança de nível
    Dado uma configuração de protocolo vigente
    E um protocolo pronto para escalonamento
    Quando o sistema inicia o escalonamento
    Então o tutor deve ser notificado

  Cenário: Esgotados os níveis, o protocolo é encerrado por esgotamento
    Dado uma configuração de protocolo vigente
    E um protocolo pronto para escalonamento
    Quando o sistema avança o escalonamento 5 vezes
    Então o protocolo deve ficar com status "ENCERRADO_POR_ESGOTAMENTO"
