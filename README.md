#  API de Gerenciamento de Produtos com Descontos como TESTE do FIEPE para Desenvolvedor Full Stack

Este projeto consiste em uma API RESTful desenvolvida com Spring Boot, voltada para o gerenciamento de produtos, incluindo controle de estoque e aplicação de descontos —
seja por percentual ou por cupom. Além das operações básicas de CRUD, a aplicação oferece recursos adicionais como filtragem, exclusão lógica (soft delete) e atualizações parciais por meio de JSON Patch, com 
embarcamento Docker e usando as boas práticas de Clean Code e Design Pattern
---

## Índice

1. [Funcionalidades](#funcionalidades)  
2. [Tecnologias](#tecnologias)  
3. [Estrutura do Projeto](#estrutura-do-projeto)  
4. [Como rodar o projeto localmente](#como-rodar-o-projeto-localmente)  
5. [Arquivo de variáveis de ambiente](#arquivo-de-variáveis-de-ambiente)  
6. [Executando com Docker](#executando-com-docker)  
7. [Comandos Docker Úteis](#comandos-docker-úteis)  
8. [Dockerfiles](#dockerfiles)  
9. [Testes](#testes)  
10. [Licença](#licença)  

# Funcionalidades

- CRUD completo de produtos
- Aplicação de desconto percentual
- Aplicação e remoção de cupons de desconto
- Inativação e restauração de produtos (soft delete)
- Filtro de listagem com diversos parâmetros
- Atualização parcial com suporte a JSON Patch
- Tratamento centralizado de exceções
- Cobertura de testes com MockMvc


# Tecnologias Utilizadas

- Java 17+
- Spring Boot
- Spring Data JPA
- Spring Web + Validation
- Jackson + JSON Patch
- H2 Database (padrão) e PostgreSQL (opcional)
- JUnit 5 + Mockito + MockMvc
- Docker
- Bootstrap


# 📁 Estrutura do Projeto
# BackEnd
src/
├── main/
│   ├── java/com/cesar/
│   │   ├── controller/            # Camada REST: mapeamento dos endpoints
│   │   ├── dto/                   # Objetos de transferência de dados (Request e Response)
│   │   ├── exception/             # Exceções personalizadas e handler global
│   │   ├── model/                 # Entidades JPA: Product, ProductDiscount, etc.
│   │   ├── repository/            # Interfaces JPA para acesso a dados
│   │   ├── service/               # Regras de negócio e lógica de aplicação
│   │   └── util/                  # Conversores e utilidades (ex: ProductConverter)
│   └── resources/
│       ├── application.properties # Configurações da aplicação (porta, banco, etc.)
│       └── data.sql               # (Opcional) carga inicial de dados
└── test/
    └── com/cesar/
        └── ProductControllerTest.java  # Testes com MockMvc simulando requisições reais
         └── ProductServiceTest.java  # Testes com MockMvc 


frontend/
├── src/
│   ├── app/
│   │   ├── components/            # Componentes reutilizáveis (cards, botões, modais)
│   │   │   └── product-card/      # Ex: product-card.component.ts/.html/.scss
│   │   ├── pages/                 # Views/rotas principais (listagem, detalhes, formulário)
│   │   │   ├── product-list/      # product-list.component.ts/.html/.scss
│   │   │   └── product-edit/      # product-edit.component.ts/.html/.scss
│   │   ├── services/              # Serviços HTTP para consumir a API
│   │   │   └── product.service.ts
│   │   ├── models/                # Interfaces e tipos (Product.ts, Discount.ts)
│   │   ├── enviroments/           # Emarbacamento do Docker
│   │   ├── interceptors/          # Interceptadores HTTP (Auth, erros)
│   │   ├── guards/                # Guards de rota (ex: autenticação)
│   │   ├── app-routing.module.ts  # Definição de rotas da aplicação
│   │   └── app.module.ts          # Módulo principal e imports de libs
│   ├── assets/                    # Imagens, fontes e ícones
│   ├── environments/              # Configurações de ambiente (dev, prod)
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── index.html                 # Template HTML principal
│   ├── main.ts                    # Bootstrap da aplicação
│   └── styles.scss                # Estilos globais
├── angular.json                   # Configurações do CLI Angular
├── package.json                   # Dependências e scripts
├── tsconfig.json                  # Configurações TypeScript
└── README.md                      # Documentação do front-end


# Pontos-chaves

- Organização por **componentes** e **pages**:  
  Cada componente (`.ts/.html/.scss`) é atômico e reutilizável; as pages representam rotas inteiras.  
- Services puros para consumo da API:  
  Chamadas HTTP centralizadas em `ProductService`, `AuthService`, etc.  
- Models/DTOs fortemente tipados:  
  Interfaces definem o shape dos objetos, facilitando a compreensão e o autocomplete.  
- Interceptors e Guards:  
  - **AuthInterceptor** para injetar token JWT.  
  - **ErrorInterceptor** para tratamento global de erros.  
  - **AuthGuard** para proteger rotas.  
- SCSS modular:  
  Uso de variáveis e mixins em `styles.scss` para manter consistência visual.  
- Internacionalização futura:  
  Estrutura pronta para adicionar `@ngx-translate/core`.

# Principais Dependências

| Biblioteca                  | Finalidade                                 |
|-----------------------------|--------------------------------------------|
| @angular/material           | Componentes UI (botões, tabelas, dialogs)  |
| @angular/forms              | Formulários reativos e validações          |
| rxjs                        | Programação reativa e tratamento de streams |
| ngx-mask                    | Máscaras de input (CPF, CNPJ, CEP, moeda)  |
| ng2-currency-mask           | Máscara e formatação de valores monetários |
| ngx-toastr                  | Notificações toast                         |
| sweetalert2                 | Pop-ups e confirmações estilizadas         |
| @auth0/angular-jwt          | Gestão e decodificação de JWT              |
| @ngx-translate/core         | Internacionalização (i18n)                 |
| ngx-pagination              | Paginação de listas                        |


# Testes Automatizados

Testamos os principais fluxos da aplicação usando **MockMvc** com cobertura de:

- ✔️ Produto encontrado por ID
- ❌ Produto não encontrado (404)
- ✔️ Inativar produto (204 / 404)
- ✔️ Restaurar produto (200 / 404)
- ✔️ Aplicar desconto por cupom (200 / 404 / 400)
- ✔️ Filtros com `hasDiscount=true`
- ❌ Atualização parcial via PATCH (em progresso)
- ✔️ Atualizar produto com ID inválido (404)

# TESTES - Cobertura Atual

- **Classes:** 63%
- **Métodos:** 51%
- **Linhas:** 37%


# Executando o Projeto

# Pré-requisitos

- Java 17 instalado
- Maven (ou IDE com suporte a Spring Boot)

# Passos

# 1. Clone o repositório:

 - ```bash
git clone https://github.com/seu-usuario/seu-repo.git
cd seu-repo

# 2. Rode o projeto:

bash
./mvnw spring-boot:run

# 3. Acesse a API:

mvn clean install
mvn spring-boot:run
http://localhost:8080/products

# 4. Para o Front

cd ../FrontEnd
npm install
ng serve --open
Acesse http://localhost:4200
 
 
 # Executando com Docker
Pré-requisitos
Docker >= 20.10

Docker Compose >= 1.29

1. Clone o repositório
bash
git clone https://github.com/seu-usuario/DesafioFullStackFiepe.git
cd DesafioFullStackFiepe



# Exemplos de Uso
  GET /products
http
GET /products
POST /products
json
{
  "name": "Teclado Mecânico",
  "description": "Com RGB e switch blue",
  "price": 220.0,
  "stock": 10
}
PATCH /products/{id}
json
[
  { "op": "replace", "path": "/name", "value": "Nome Atualizado" }

# Extras 
Tratamento Global de Erros
Incluímos um @ControllerAdvice (GlobalExceptionHandler) que captura exceções como:

NotFoundException → 404

IllegalArgumentException → 400

Outras genéricas → 500


# Comandos Docker Úteis

# Parar e remover containers, rede e volumes
docker-compose down --volumes

# Listar containers em execução
docker ps

# Exibir logs em tempo real (ex.: backend)
docker-compose logs -f backend

# Reconstruir e subir somente um serviço (ex.: frontend)
docker-compose build frontend
docker-compose up -d frontend

# BackEnd/Dockerfile

FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

# FrontEnd/Dockerfile

# build
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --prod

# serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
ENTRYPOINT ["nginx","-g","daemon off;"]
___

# Conversores DTO
Todos os dados expostos ao cliente são convertidos usando o ProductConverter, garantindo desacoplamento entre a camada de domínio e a apresentação.


# Licença
O uso é livre para finalidades academicas.

