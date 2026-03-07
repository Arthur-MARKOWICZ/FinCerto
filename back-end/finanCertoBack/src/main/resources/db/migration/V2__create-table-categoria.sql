-- Criar tabela de categorias
CREATE TABLE tb_categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(255) NOT NULL CHECK (tipo IN ('INCOME', 'EXPENSE')),
    usuario_id BIGINT NOT NULL,
    
    CONSTRAINT fk_categoria_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES tb_usuario(id) 
        ON DELETE CASCADE
);

-- Criar constraint unique para nome + usuario_id
ALTER TABLE tb_categoria 
ADD CONSTRAINT uk_categoria_usuario_nome 
UNIQUE (usuario_id, nome);

-- Criar índice para busca por usuário
CREATE INDEX idx_categoria_usuario_id ON tb_categoria(usuario_id);
