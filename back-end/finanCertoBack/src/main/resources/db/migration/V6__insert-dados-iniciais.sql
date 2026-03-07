-- Inserir dados iniciais para testes

-- Inserir usuário de teste
INSERT INTO tb_usuario (nome, email, senha) VALUES 
('Usuário Teste', 'teste@financerto.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK'); -- senha: 123456

-- Inserir categorias padrão para o usuário de teste
INSERT INTO tb_categoria (nome, tipo, usuario_id) VALUES 
('Salário', 'INCOME', 1),
('Alimentação', 'EXPENSE', 1),
('Transporte', 'EXPENSE', 1),
('Moradia', 'EXPENSE', 1),
('Lazer', 'EXPENSE', 1),
('Saúde', 'EXPENSE', 1),
('Educação', 'EXPENSE', 1),
('Outros', 'EXPENSE', 1);

-- Inserir contas para o usuário de teste
INSERT INTO tb_conta (nome, tipo, saldo_inicial, usuario_id) VALUES 
('Conta Corrente', 'CONTA_CORRENTE', 5000.00, 1),
('Poupança', 'POUPANCA', 10000.00, 1),
('Cartão de Crédito', 'CARTAO', 0.00, 1),
('Dinheiro', 'DINHEIRO', 500.00, 1);

-- Inserir transações de exemplo para o usuário de teste
INSERT INTO tb_transacao (valor, date, descricao, tipo, conta_id, categoria_id, usuario_id) VALUES 
(5000.00, '2026-01-05 09:00:00', 'Salário mensal', 'INCOME', 1, 1, 1),
(1500.00, '2026-01-10 14:30:00', 'Supermercado', 'EXPENSE', 1, 2, 1),
(200.00, '2026-01-12 08:15:00', 'Combustível', 'EXPENSE', 4, 3, 1),
(1200.00, '2026-01-15 10:00:00', 'Aluguel', 'EXPENSE', 1, 4, 1),
(300.00, '2026-01-20 19:00:00', 'Restaurante', 'EXPENSE', 1, 5, 1),
(150.00, '2026-01-22 16:45:00', 'Farmácia', 'EXPENSE', 4, 6, 1),
(500.00, '2026-01-25 11:30:00', 'Curso online', 'EXPENSE', 1, 7, 1);

-- Inserir orçamentos de exemplo para o usuário de teste
INSERT INTO tb_orcamento (valor_limite, valor_atual, nome, prazo, categoria_id, usuario_id) VALUES 
(2000.00, 1500.00, 'Orçamento Alimentação', '2026-01-31', 2, 1),
(500.00, 200.00, 'Orçamento Transporte', '2026-01-31', 3, 1),
(1500.00, 1200.00, 'Orçamento Moradia', '2026-01-31', 4, 1),
(400.00, 300.00, 'Orçamento Lazer', '2026-01-31', 5, 1),
(200.00, 150.00, 'Orçamento Saúde', '2026-01-31', 6, 1),
(600.00, 500.00, 'Orçamento Educação', '2026-01-31', 7, 1);
