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

Todos os endpoints (exceto `/api/login`) requerem autenticação.

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/login` | Autenticação |
| POST | `/api/divergence` | Registrar divergências |
| GET | `/api/divergence` | Listar por data |
| GET | `/api/divergence/query` | Consultar produto |
| PUT | `/api/divergence/{id}` | Editar divergência |
| DELETE | `/api/divergence/{id}` | Remover divergência |
| GET | `/api/divergence/report/pdf` | Gerar relatório PDF |
| GET | `/api/divergence/ranking/separator` | Ranking por separador |
| GET | `/api/divergence/ranking/product` | Ranking por produto |
| POST | `/api/import/products` | Importar produtos CSV |
| POST | `/api/import/separation-products` | Importar separações CSV |
| GET | `/api/old-pending` | Pendências antigas |
| GET | `/api/status/{jobId}` | Status de importação |