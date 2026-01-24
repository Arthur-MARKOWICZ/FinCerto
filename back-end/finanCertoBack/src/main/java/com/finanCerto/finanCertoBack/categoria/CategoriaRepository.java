package com.finanCerto.finanCertoBack.categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    boolean existsByUsuarioIdAndNome(Long usuarioId, String nome);

    Optional<Categoria> findByNome(String nome);
    
    Page<Categoria> findByUsuarioId(Long usuarioId, Pageable pageable);
}
