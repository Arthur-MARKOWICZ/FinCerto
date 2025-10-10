# FinanCerto — Sistema de Controle Financeiro Pessoal

 Projeto desenvolvido com o objetivo de estudo e aprimoramento profissional, aplicando conceitos de Java, Spring Boot, autenticação JWT, segurança e boas práticas de arquitetura.
Criado para demonstrar domínio de back-end em um cenário real de API financeira moderna.

## Descrição do Projeto

O FinanCerto é uma API REST voltada para o gerenciamento financeiro pessoal, permitindo que o usuário cadastre suas contas e categorias de despesas/receitas, além de autenticar-se com segurança via JWT.

O projeto simula um ambiente profissional, com autenticação, validações, logs e arquitetura em camadas, pronto para evoluir com novas funcionalidades como relatórios e controle de transações.
## Objetivos do Projeto

Consolidar conhecimentos em Java + Spring Boot

Aplicar boas práticas de arquitetura em camadas (Controller, Service, Repository)

Implementar autenticação JWT com Spring Security

Praticar persistência de dados com JPA/Hibernate

Utilizar Docker Compose para orquestrar banco de dados PostgreSQL

Criar uma base sólida para futuras implementações de transações e relatórios financeiros

## ⚙️ Tecnologias Utilizadas
Categoria	Tecnologias
Linguagem	Java 17+
Framework	Spring Boot
Banco de Dados	PostgreSQL (via Docker Compose)
ORM	Hibernate / JPA
Segurança	Spring Security + JWT
Documentação	Swagger / Springdoc OpenAPI
Build Tool	Maven
Logs	SLF4J + Logback


## ✅ Funcionalidades Implementadas

 Cadastro de usuário

 Login com autenticação JWT

 Configuração de segurança (Spring Security + Token Service)

 Cadastro de conta financeira

 Obtenção de conta por nome (único por usuário)

 Listagem de todas as contas por ID de usuário

 Atualização de conta

 Consulta de saldo da conta

 Cadastro de categoria

## 🚧 Funcionalidades em Desenvolvimento

 Relatórios financeiros

 Controle completo de transações (receitas e despesas)

 Módulo de orçamento mensal

 Testes automatizados (JUnit + Mockito)

 Deploy na nuvem (Render / Railway / AWS)

💡 O projeto está em constante evolução, refletindo o processo de aprendizado e aprimoramento contínuo em desenvolvimento back-end profissional.

## 🧾 Como Executar o Projeto
### 🔧 Pré-requisitos

Java 21

Maven

Docker e Docker Compose

### ▶️ Passos para rodar

Clone o repositório:
``` bash
git clone https://github.com/seuusuario/financerto.git
```

Acesse o diretório:
``` bash
cd financerto
```

Suba o banco de dados com Docker Compose:
``` bash
docker-compose up -d
```

Execute o projeto:
``` bash
mvn spring-boot:run
```

Acesse o Swagger:

http://localhost:8080/swagger-ui/index.html

## 🧠 Conceitos Aplicados

Autenticação JWT e segurança de endpoints

Padrão DTO e validação de dados

Injeção de dependência e inversão de controle

Organização modular em camadas

Boas práticas de código limpo e reutilizável

Uso de Docker Compose para infraestrutura

## 👨‍💻 Autor

Arthur Markowicz Lopes
💼 Desenvolvedor Back-End em formação

📚 Foco em Java, Spring Boot e Arquitetura de Sistemas

🔗 [LinkedIn](https://www.linkedin.com/in/arthur-markowicz-lopes)

💻 [GitHub](https://github.com/Arthur-MARKOWICZ)

## 🪄 Considerações Finais

O FinanCerto é um projeto pessoal criado para aprimorar habilidades técnicas e demonstrar conhecimento em tecnologias amplamente utilizadas no mercado.
Mesmo sendo um projeto de estudo, ele segue padrões profissionais de código e arquitetura, servindo como portfólio prático para oportunidades na área de desenvolvimento back-end.

## 💬 Licença

Este projeto é de uso livre para fins educacionais.
Sinta-se à vontade para clonar, estudar e contribuir com melhorias!
