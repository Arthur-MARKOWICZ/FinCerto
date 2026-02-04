-- Criar tabela de orçamentos
CREATE TABLE tb_orcamento (
    id BIGSERIAL PRIMARY KEY,
    valor_limite DOUBLE PRECISION NOT NULL,
    valor_atual DOUBLE PRECISION DEFAULT 0.0,
    nome VARCHAR(255) NOT NULL,
    prazo DATE NOT NULL,
    categoria_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    CONSTRAINT fk_orcamento_categoria 
        FOREIGN KEY (categoria_id) 
        REFERENCES tb_categoria(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_orcamento_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES tb_usuario(id) 
        ON DELETE CASCADE
);

-- Criar constraints unique
ALTER TABLE tb_orcamento 
ADD CONSTRAINT uk_orcamento_usuario_nome 
UNIQUE (usuario_id, nome);

ALTER TABLE tb_orcamento 
ADD CONSTRAINT uk_orcamento_categoria_nome 
UNIQUE (categoria_id, nome);

-- Criar índices para performance
CREATE INDEX idx_orcamento_usuario_id ON tb_orcamento(usuario_id);
CREATE INDEX idx_orcamento_categoria_id ON tb_orcamento(categoria_id);
CREATE INDEX idx_orcamento_prazo ON tb_orcamento(prazo);
