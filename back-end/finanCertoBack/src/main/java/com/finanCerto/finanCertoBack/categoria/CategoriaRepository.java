package com.finanCerto.finanCertoBack.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    boolean existsByUsuarioIdAndNome(Long usuarioId, String nome);
}
