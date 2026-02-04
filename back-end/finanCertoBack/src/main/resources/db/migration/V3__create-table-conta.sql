-- Criar tabela de contas
CREATE TABLE tb_conta (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(255) CHECK (tipo IN ('CONTA_CORRENTE', 'POUPANCA', 'CARTAO', 'DINHEIRO', 'OUTROS')),
    saldo_inicial DOUBLE PRECISION NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    CONSTRAINT fk_conta_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES tb_usuario(id) 
        ON DELETE CASCADE
);

-- Criar constraint unique para nome + usuario_id
ALTER TABLE tb_conta 
ADD CONSTRAINT uk_conta_usuario_nome 
UNIQUE (usuario_id, nome);

-- Criar índice para busca por usuário
CREATE INDEX idx_conta_usuario_id ON tb_conta(usuario_id);
