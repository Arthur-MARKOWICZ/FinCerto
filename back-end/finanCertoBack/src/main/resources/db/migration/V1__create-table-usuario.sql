-- Criar tabela de usuários
CREATE TABLE tb_usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

-- Criar índice único para email
CREATE UNIQUE INDEX idx_usuario_email ON tb_usuario(email);
