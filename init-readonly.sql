-- Cria usuário somente leitura
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles WHERE rolname = 'readonly_user'
   ) THEN
      CREATE ROLE readonly_user LOGIN PASSWORD 'readonly_password';
   END IF;
END
$$;

-- Permissão de conexão
GRANT CONNECT ON DATABASE finanCerto TO readonly_user;

\c finanCerto

-- Permissões no schema
GRANT USAGE ON SCHEMA public TO readonly_user;

-- Permissão de leitura nas tabelas existentes
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;

-- Permissão automática para tabelas futuras
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO readonly_user;
