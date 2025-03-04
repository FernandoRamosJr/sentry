# Projeto de Gerenciamento de Documentos

Este projeto é uma API RESTful desenvolvida com **Spring Boot** e **Maven** para gerenciar documentos através de operações de criação, consulta, atualização e exclusão. O sistema armazena arquivos no sistema de arquivos e registra os metadados correspondentes no banco de dados. Além disso, a API conta com segurança via API Key, logging robusto com log4j, uma tarefa agendada para monitoramento e documentação interativa com Swagger.

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Arquitetura e Stack Tecnológica](#arquitetura-e-stack-tecnológica)
- [Endpoints da API](#endpoints-da-api)
    - [Criar Documento](#criar-documento)
    - [Retornar Documento](#retornar-documento)
    - [Atualizar Documento](#atualizar-documento)
    - [Apagar Documento](#apagar-documento)
- [Armazenamento e Manipulação de Arquivos](#armazenamento-e-manipulação-de-arquivos)
- [Tarefa Agendada](#tarefa-agendada)
- [Segurança e Documentação](#segurança-e-documentação)
- [TDD e Manutenibilidade](#tdd-e-manutenibilidade)
- [Considerações Finais](#considerações-finais)

---

## Funcionalidades

- **Criação de Documentos:** Recebe o nome e o arquivo binário, registra os metadados no banco e armazena o arquivo no sistema de arquivos.
- **Consulta de Documentos:** Retorna o arquivo armazenado para download, baseado no ID.
- **Atualização de Documentos:** Permite atualizar o nome e/ou o arquivo associado, substituindo o antigo arquivo no sistema.
- **Exclusão de Documentos:** Remove o registro do banco de dados e exclui o arquivo do sistema.
- **Monitoramento:** Uma tarefa agendada gera logs diários com o número de arquivos armazenados e o total em bytes.
- **Segurança:** A API é protegida via API Key e o acesso aos endpoints é limitado conforme configurado.
- **Documentação Interativa:** Uso de Swagger para a documentação dos endpoints.
- **Testes:** Desenvolvimento orientado por testes (TDD) garantindo robustez e qualidade do código.

---

## Arquitetura e Stack Tecnológica

- **Spring Boot:** Facilita a configuração e o desenvolvimento da aplicação.
- **Maven:** Gerenciamento de dependências e build do projeto.
- **Spring Data:** Abstração para operações com o banco de dados.
- **API Restful:** Interface para operações CRUD.
- **Log4j:** Logging detalhado para rastreamento e debugging.
- **Spring Security:** Implementa a segurança via API Key para acesso à API.
- **Agendamento de Tarefas:** Utilização do `@Scheduled` e `@Async` para tarefas agendadas.
- **Swagger:** Documentação e testes interativos da API.

---

## Endpoints da API

### Criar Documento

- **Descrição:** Cria um novo documento, salvando os metadados no banco e o arquivo no sistema de arquivos.
- **Recebe:**
    - `nome` (String): Nome do documento.
    - `file` (MultipartFile): Arquivo binário.
- **Retorna:** Status e ID do documento criado.
- **Fluxo:**
    1. Validação se o arquivo não está vazio.
    2. Criação e salvamento do registro para geração do ID.
    3. Geração de um nome único e armazenamento do arquivo.
    4. Atualização do registro com o caminho, nome final e tamanho do arquivo.

### Retornar Documento

- **Descrição:** Retorna o documento para download com base no ID.
- **Recebe:**
    - `id` (PathVariable): ID do documento.
- **Retorna:** O arquivo armazenado e status HTTP adequado.
- **Fluxo:**
    1. Busca do registro no banco.
    2. Criação de um objeto `Resource` a partir do caminho armazenado.
    3. Verificação de existência e legibilidade do arquivo.
    4. Configuração do header `Content-Disposition` para download.

### Atualizar Documento

- **Descrição:** Atualiza o nome e/ou o arquivo do documento.
- **Recebe:**
    - `id` (PathVariable): ID do documento.
    - `nome` (opcional): Novo nome para o documento.
    - `file` (opcional): Novo arquivo binário.
- **Retorna:** Status da operação.
- **Fluxo:**
    1. Busca do documento pelo ID.
    2. Atualização do nome se fornecido.
    3. Se um novo arquivo for enviado:
        - Exclusão do arquivo antigo, caso exista.
        - Armazenamento do novo arquivo e atualização dos metadados.

### Apagar Documento

- **Descrição:** Remove o documento tanto do banco de dados quanto do sistema de arquivos.
- **Recebe:**
    - `id` (PathVariable): ID do documento.
- **Retorna:** Status da operação.
- **Fluxo:**
    1. Verificação e busca do documento pelo ID.
    2. Exclusão do arquivo físico, se existir.
    3. Remoção do registro do banco de dados.
---

## Tarefa Agendada

- **Objetivo:** Registrar diariamente, via log, o número de documentos armazenados e o total de bytes.
- **Implementação:**
    - Utiliza `@Scheduled` para execução diária (configurado para as 20:30).
    - O método `logDocumentoStatus` conta os documentos e soma os tamanhos.
    - A anotação `@Async` permite execução assíncrona sem bloquear outras operações.

---

## Segurança e Documentação

- **Segurança via API Key:**
    - A anotação `@SecurityRequirement(name = "ApiKeyAuth")` indica a proteção dos endpoints, garantindo que apenas requisições autenticadas possam acessar os recursos.
- **Documentação com Swagger:**
    - A API é documentada de forma interativa, permitindo que desenvolvedores testem os endpoints facilmente e compreendam os parâmetros e respostas.

---

## TDD e Manutenibilidade

- **Test-Driven Development:**
    - A estrutura modular (controllers, services, utilitários) foi pensada para facilitar a criação e manutenção de testes unitários e de integração.
    - Garante a robustez e a confiabilidade da aplicação, aspectos essenciais para um ambiente de produção.

---

## Considerações Finais

Este projeto atende aos requisitos propostos de forma elegante e escalável:
- **Modularidade:** Separação clara entre a lógica de negócio, a camada de apresentação e a manipulação de arquivos.
- **Segurança e Monitoramento:** Implementação de segurança via API Key e tarefas agendadas para monitoramento, agregando valor ao sistema.
- **Documentação e Testabilidade:** Uso de Swagger para documentação e TDD para garantir a qualidade do código.
- **Tecnologias Modernas:** Utilização de Spring Boot, Spring Data, e outras tecnologias que promovem desenvolvimento ágil e sustentável.

---
