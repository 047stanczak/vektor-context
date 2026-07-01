# Changelog

## [1.1.0] - 2026-06-30

### Adicionado
- Página inicial em `/vektor` com lista de tarefas (toollist)
- Módulo de contagem/auditoria com geração de PDF
- Leitor de QR/código de barras para consulta de pendências
- Endpoints de auditoria (`/api/audit`)
- Endpoints de tarefas (`/api/tasks`)
- Endpoints de contagem (`/api/counting/*`)
- Endpoint `/api/pending-by-barcode` e `/api/pending-by-code`
- Tratamento global de exceções via `@RestControllerAdvice`
- Documentação Swagger/OpenAPI, controlável via `SWAGGER_ENABLED`
- Variável `COOKIE_SECURE` para controle de cookie seguro entre ambientes

### Alterado
- Controllers refatorados para usar exceções customizadas (`ProductNotFoundException`, `DivergenceNotFoundException`, `UserAlreadyExistsException`)
- Removida dependência `spring-boot-starter-webmvc-test` (conflito de versão)
- Testes agora usam banco H2 em memória

### Removido
- Dependências não utilizadas: `@radix-ui/react-select`, `date-fns` (frontend), `poi`/`poi-ooxml` (backend)