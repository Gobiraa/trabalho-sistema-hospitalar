# Decisões técnicas – Sprint 2

## Banco de dados: relacional (JPA/Hibernate)

Escolhemos um banco **relacional**. Padrão de desenvolvimento: **H2** em arquivo (sem instalação);
perfil `postgres` pronto para **PostgreSQL**.

**Justificativa**
- O domínio é fortemente relacional (paciente ↔ atendimentos ↔ profissional; internação ↔ quarto) e exige
  **integridade referencial** (regra de negócio 7): chaves estrangeiras impedem atendimentos órfãos.
- Regras como "quarto não ultrapassa a capacidade" e "profissional sem dois atendimentos no mesmo horário"
  precisam de **transações** e controle de concorrência. O `Quarto` usa `@Version` (bloqueio otimista).
- CPF e registro profissional têm restrição `UNIQUE` no banco, além da validação na aplicação.
- Consultas de histórico (por paciente, por profissional, por especialidade) são junções simples em SQL.
- MongoDB traria flexibilidade de esquema que não é necessária: os dados são estruturados e estáveis.

## Mapeamento da hierarquia

| Hierarquia | Estratégia | Motivo |
|---|---|---|
| `ProfissionalSaude` → `Medico` → `MedicoClinicoGeral` / `MedicoEspecialista` / `Cirurgiao` | `JOINED` | Cada categoria tem colunas próprias; evita muitas colunas nulas. |
| `Atendimento` → `Consulta` → `ConsultaRotina` / `ConsultaEspecializada`; `AtendimentoEmergencia`; `ProcedimentoCirurgico` | `JOINED` | Permite consultar todos os atendimentos de um profissional/paciente em uma só consulta polimórfica (usada na regra de horários). |
| `Pessoa` | `@MappedSuperclass` | Só compartilha atributos; não precisa ser consultada isoladamente. |

## Como cada requisito da Sprint foi atendido

- **Categorias de profissionais** – hierarquia acima; `categoria` define o tipo no `POST /profissionais`.
  `GET /profissionais?especialidade=CARDIOLOGIA[&data=…&horario=…]` lista por área e, opcionalmente, só os disponíveis.
  Clínico geral encaminha via `POST /consultas/{id}/encaminhamento`. Cirurgião tem procedimentos em
  `GET /profissionais/{id}/procedimentos`.
- **Modalidades de atendimento** – `Atendimento` concentra o que é comum (paciente, profissional, data, horário,
  observações). Cada modalidade implementa seu próprio `validarRealizacao()`:
  - rotina: exige médico clínico geral;
  - especializada: exige especialista cuja especialidade seja igual à da consulta;
  - emergência: exige prioridade; fila ordenada por prioridade; separa aguardando/em atendimento/concluído;
  - procedimento cirúrgico: exige cirurgião; só conclui se foi iniciado.
- **Situação da consulta** – `enum StatusConsulta` com a tabela de transições dentro do próprio enum
  (`proximosPermitidos()`); nenhum texto livre. Concluída/cancelada/não compareceu são estados finais;
  para voltar a atender é preciso um **novo agendamento**.
- **Tipos de quarto** – `enum TipoQuarto` com limite de capacidade por tipo (enfermaria 6, apartamento 2, UTI 1, isolamento 1).
  A situação (`DISPONIVEL`/`OCUPADO`) é **derivada** da ocupação, nunca gravada à mão.
- **Histórico (regra 6)** – consultas e internações não têm `DELETE`; cancela-se ou dá-se alta.
  Pacientes/profissionais/quartos com histórico não podem ser removidos.

## Decisões de interpretação (podem ser ajustadas)

- Emergências não entram no controle de horário do profissional (são imediatas, e podem nem ter médico ainda).
- "Mesmo horário" = mesma data e mesma hora de início; consultas canceladas ou "não compareceu" liberam o horário.
- Não se agenda no passado.
- Um paciente não pode ter duas internações ativas ao mesmo tempo.
- Cirurgião é um tipo de médico (herda de `Medico`), conforme "além das informações comuns aos médicos".

## Códigos HTTP

`400` dados inválidos · `404` não encontrado · `409` conflito (horário ocupado, quarto lotado, duplicidade) ·
`422` regra de negócio violada (transição inválida, profissional incompatível, CPF inválido).
