-- Criar tabela de transações
CREATE TABLE tb_transacao (
    id BIGSERIAL PRIMARY KEY,
    valor DOUBLE PRECISION NOT NULL,
    date TIMESTAMP NOT NULL,
    descricao VARCHAR(255),
    tipo VARCHAR(255) NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    conta_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    CONSTRAINT fk_transacao_conta 
        FOREIGN KEY (conta_id) 
        REFERENCES tb_conta(id) 
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_transacao_categoria 
        FOREIGN KEY (categoria_id) 
        REFERENCES tb_categoria(id) 
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_transacao_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES tb_usuario(id) 
        ON DELETE CASCADE
);

-- Criar índices para performance
CREATE INDEX idx_transacao_usuario_id ON tb_transacao(usuario_id);
CREATE INDEX idx_transacao_conta_id ON tb_transacao(conta_id);
CREATE INDEX idx_transacao_categoria_id ON tb_transacao(categoria_id);
CREATE INDEX idx_transacao_date ON tb_transacao(date);
CREATE INDEX idx_transacao_tipo ON tb_transacao(tipo);
