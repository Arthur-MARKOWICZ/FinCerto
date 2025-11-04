# 💰 FinanCerto — Sistema de Controle Financeiro Pessoal

![Java](https://img.shields.io/badge/Java-21-red?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)
![Python](https://img.shields.io/badge/Python-3.13-yellow?logo=python)
![FastAPI](https://img.shields.io/badge/FastAPI-0.115-green?logo=fastapi)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

> API REST desenvolvida em **Java + Spring Boot** para gerenciamento financeiro pessoal, com **autenticação JWT**, **boas práticas de arquitetura**, **integração Docker + PostgreSQL**, e **módulo de relatórios em Python (FastAPI)**.  
>  
> Criado como projeto de estudo e portfólio, o **FinanCerto** simula um ambiente de **API financeira profissional**, com segurança, modularidade e escalabilidade.

---

## 🧩 Visão Geral

O **FinanCerto** é uma aplicação back-end que permite o controle financeiro pessoal por meio do cadastro de **usuários, contas, categorias e transações**.  
O sistema utiliza **autenticação JWT** para garantir segurança e isolar dados por usuário, aplicando padrões sólidos de desenvolvimento **(Controller → Service → Repository)**.

A arquitetura foi planejada para ser **modular e extensível**, permitindo a inclusão de novos serviços, como **relatórios financeiros, orçamentos e dashboards analíticos**.  
O módulo de **relatórios** foi desenvolvido em **Python (FastAPI)**, comunicando-se com o core em **Spring Boot**.

---

## 🎯 Objetivos do Projeto

- Consolidar conhecimentos em **Java + Spring Boot**
- Aplicar **boas práticas de arquitetura em camadas**
- Implementar **autenticação JWT com Spring Security**
- Praticar **persistência de dados** com **JPA/Hibernate**
- Utilizar **Docker Compose** para orquestração do PostgreSQL
- Criar uma base sólida para **módulos de transações e relatórios**
- Integrar **serviços entre Java e Python (FastAPI)** de forma desacoplada

---

## ⚙️ Tecnologias Utilizadas

| Categoria | Tecnologias |
|------------|-------------|
| **Linguagem** | Java 21, Python 3.13 |
| **Frameworks** | Spring Boot, FastAPI |
| **Banco de Dados** | PostgreSQL (via Docker Compose) |
| **ORM** | Hibernate / JPA |
| **Segurança** | Spring Security + JWT |
| **Documentação** | Swagger / Springdoc OpenAPI |
| **Build Tool** | Maven |
| **Logs** | SLF4J + Logback |
| **Infraestrutura** | Docker e Docker Compose |

---

## ✅ Funcionalidades Implementadas

- 👤 **Cadastro e autenticação de usuários (JWT)**
- 💳 **Cadastro e listagem de contas financeiras**
- 🔍 **Busca de conta por nome (único por usuário)**
- 💰 **Consulta e atualização de saldo**
- 🧾 **Cadastro de categorias**
- 💸 **Cadastro de transações**
- 🔐 **Configuração completa de segurança (Spring Security + Token Service)**
- 📊 **Geração de relatórios financeiros personalizados (módulo FastAPI)**
- ☁️ **Deploy em nuvem (Oracle Cloud)**  

---

## 🚧 Em Desenvolvimento

- 💵 **Controle completo de receitas e despesas**  
- 📅 **Módulo de orçamento mensal**  
- 🧪 **Testes automatizados (JUnit + Mockito)**  

💡 *O projeto está em constante evolução, refletindo o processo de aprendizado contínuo em desenvolvimento back-end profissional.*

---

## 🧠 Conceitos e Boas Práticas Aplicadas

- Autenticação e autorização via **JWT**
- Padrão **DTO** e **validação de dados**
- **Injeção de dependência** e **Inversão de Controle (IoC)**
- Arquitetura **modular e desacoplada**
- **Logs estruturados** com SLF4J + Logback
- Uso de **Docker Compose** para infraestrutura local
- **Boas práticas de Clean Code** e **organização por camadas**
- **Comunicação entre serviços** (Spring Boot ↔️ FastAPI)

---

## ▶️ Como Executar o Projeto

### 🔧 Pré-requisitos

- Java 21  
- Maven  
- Python 3.13  
- Docker e Docker Compose

---

### 🧭 Passos

```bash
# 1. Clonar o repositório
git clone https://github.com/seuusuario/financerto.git

# 2. Acessar o diretório
cd financerto

# 3. Subir o banco de dados com Docker
docker-compose up -d

# 4. Executar o back-end Java
mvn spring-boot:run

# 5. (Opcional) Executar o módulo de relatórios em Python
cd financerto-analytics
uvicorn app.main:app --reload --port 8000
```
## 🌐 Acessar Documentação (Swagger)

👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 👨‍💻 Autor

**Arthur Markowicz Lopes**  
💼 Desenvolvedor Back-End em formação  
📚 Foco em **Java**, **Spring Boot** e **Arquitetura de Sistemas**

🔗 [**LinkedIn**](https://www.linkedin.com/in/arthur-markowicz-lopes/)  
💻 [**GitHub**](https://github.com/arthurmarkowiczlopes)

---

## 🪄 Considerações Finais

O **FinanCerto** é mais do que um projeto de estudo — é um **laboratório de boas práticas em desenvolvimento back-end**.  
Cada módulo representa uma etapa de aprendizado em **arquitetura limpa**, **segurança**, **integração** e **escalabilidade**.

🚀 Mesmo em constante evolução, o projeto já reflete **padrões profissionais** e serve como **portfólio prático** para oportunidades na área de **desenvolvimento back-end**.

---

## 🧾 Licença

Este projeto é de **uso livre para fins educacionais**.  
Sinta-se à vontade para **clonar**, **estudar** e **contribuir** com melhorias!
