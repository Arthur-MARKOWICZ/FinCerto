CREATE USER financerto_reader WITH PASSWORD 'senha_segura_aqui';
GRANT CONNECT ON DATABASE financerto TO financerto_reader;
\c financerto
GRANT USAGE ON SCHEMA public TO financerto_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO financerto_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO financerto_reader;
