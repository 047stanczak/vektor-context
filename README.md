# VektorContext

Sistema web para gestão de divergências em operações de separação de estoque. Permite registrar, consultar e analisar divergências entre o estoque físico e o separado, com importação de dados via CSV e geração de relatórios em PDF.

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21 · Spring Boot 3.5 · Spring Security · JWT |
| Frontend | React 18 · TypeScript · Tailwind CSS · Vite |
| Banco | PostgreSQL 13 |
| Infra | Docker · Docker Compose · Nginx |

## Requisitos

- Docker e Docker Compose

## Como rodar

```bash
# 1. Clone o repositório
git clone https://github.com/047stanczak/vektor-context.git
cd vektor-context

# 2. Configure as variáveis de ambiente
cp infra/.env.example infra/.env
# edite infra/.env com seus valores

# 3. Suba os containers
cd infra
docker compose up -d
```

Acesse em `http://localhost`.

## Variáveis de ambiente

Veja [`infra/.env.example`](infra/.env.example) para a lista completa de variáveis necessárias.

## Funcionalidades

- Autenticação com JWT via cookie HttpOnly
- Importação de dados via arquivos CSV (produtos, operações, separações)
- Registro e edição de divergências por data e loja
- Consulta de produtos por código ou código de barras
- Ranking de divergências por separador e por produto
- Listagem de pendências antigas (com e sem estoque)
- Geração de relatório PDF por data
- Acompanhamento de jobs de importação em tempo real

## Endpoints principais

Todos os endpoints (exceto `/api/login` e `/api/register`) requerem autenticação.

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/register` | Criar usuário |
| POST | `/api/login` | Autenticação |
| POST | `/api/logout` | Logout |
| POST | `/api/divergence` | Registrar divergências |
| GET | `/api/divergence` | Listar por data |
| GET | `/api/divergence/query` | Consultar produto |
| PUT | `/api/divergence/{id}` | Editar divergência |
| DELETE | `/api/divergence/{id}` | Remover divergência |
| GET | `/api/divergence/report/pdf` | Gerar relatório PDF |
| GET | `/api/divergence/ranking/separator` | Ranking por separador |
| GET | `/api/divergence/ranking/product` | Ranking por produto |
| POST | `/api/import/products` | Importar produtos CSV |
| POST | `/api/import/separation-operations` | Importar operações de separação CSV |
| POST | `/api/import/separated-products` | Importar produtos separados CSV |
| POST | `/api/import/separation-products` | Importar produtos de separação CSV |
| GET | `/api/status` | Listar todos os jobs de importação |
| GET | `/api/status/{jobId}` | Status de um job de importação |
| GET | `/api/old-pending` | Pendências antigas |
| GET | `/api/old-pending-with-stock` | Pendências antigas com estoque |
| GET | `/api/old-pending-no-stock` | Pendências antigas sem estoque |
| GET | `/api/pending-by-barcode` | Pendências por código de barras |
| GET | `/api/pending-by-code` | Pendências por código (produto ou barras) |
| GET | `/api/separation-operations/separators` | Listar separadores |
| GET | `/api/tasks` | Listar tarefas |
| POST | `/api/tasks` | Criar tarefa |
| PUT | `/api/tasks/{id}` | Editar tarefa |
| DELETE | `/api/tasks/{id}` | Remover tarefa |
| POST | `/api/tasks/{id}/complete` | Concluir tarefa |
| GET | `/api/audit` | Listar auditorias |
| POST | `/api/audit` | Registrar auditoria |
| GET | `/api/counting/brands` | Listar marcas (contagem) |
| GET | `/api/counting/by-brand` | Contagem por marca |
| GET | `/api/counting/departments` | Listar departamentos (contagem) |
| GET | `/api/counting/by-department` | Contagem por departamento |
| GET | `/api/counting/by-product` | Contagem por produto |
| GET | `/api/counting/brand-by-product` | Marca de um produto |
| GET | `/api/counting/search` | Buscar item de contagem |
| POST | `/api/counting/report/pdf` | Gerar relatório PDF de contagem |

## Documentação interativa (Swagger)

Disponível em `/swagger-ui.html` quando `SWAGGER_ENABLED=true` (desabilitado por padrão em produção).