# Sistema Hospitalar – Programação Modular (PUC Minas)

API REST do Sistema de Informação Hospitalar (Spring Boot 3 · Java 21+ · JPA).

## Estrutura

```
backend/            API Spring Boot (Sprint 2)
  src/main/java/br/com/hospital/
    model/          entidades e regras de domínio (+ enums)
    repository/     Spring Data JPA
    service/        regras de aplicação
    controller/     endpoints REST
    dto/            requests/responses
    exception/      exceções e handler global
  src/test/         testes unitários e de integração
frontend/           telas sem funcionalidade (Sprint 1)
docs/
  sprint1/          diagrama de classes e cartões CRC
  sprint2/          diagrama atualizado (.puml) e decisões técnicas
```

## Executar

```bash
cd backend
mvn spring-boot:run          # H2 em arquivo (./data), porta 8080
mvn test                     # 22 testes
# PostgreSQL: mvn spring-boot:run -Dspring-boot.run.profiles=postgres  (DB_USER / DB_PASSWORD)
```

## Endpoints principais

| Recurso | Endpoints |
|---|---|
| `/pacientes` | CRUD · `GET /{id}/historico` |
| `/profissionais` | CRUD · `?especialidade=&data=&horario=` · `GET /{id}/disponibilidade` · `GET /{id}/procedimentos` |
| `/consultas` | `POST` (tipo `ROTINA`/`ESPECIALIZADA`) · `GET` (filtros) · `PUT` reagendar · `POST /{id}/confirmar\|iniciar\|concluir\|cancelar\|nao-compareceu` · `PATCH /{id}/observacoes` · `POST /{id}/encaminhamento` |
| `/internacoes` | `POST` · `GET` · `PUT` · `POST /{id}/alta` |
| `/quartos` | CRUD · `?status=&tipo=` |
| `/emergencias` | `POST` · `GET ?status=` (fila por prioridade) · `POST /{id}/iniciar\|concluir` · `PATCH /{id}/prioridade` |
| `/procedimentos-cirurgicos` | `POST` · `GET` · `PUT` · `POST /{id}/iniciar\|concluir\|cancelar` |

Exemplo:

```bash
curl -X POST localhost:8080/profissionais -H "Content-Type: application/json" -d '{
  "categoria":"CLINICO_GERAL","nome":"Dra. Ana","registroProfissional":"CRM-MG 12345",
  "telefone":"31999990000","email":"ana@hospital.com"}'
```

Decisões técnicas, escolha do banco e interpretação das regras: [docs/sprint2/justificativa_banco_e_decisoes.md](docs/sprint2/justificativa_banco_e_decisoes.md).
