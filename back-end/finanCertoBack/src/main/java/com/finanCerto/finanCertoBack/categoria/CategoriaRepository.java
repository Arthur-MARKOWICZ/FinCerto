package com.finanCerto.finanCertoBack.categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByUsuarioIdAndNome(Long usuarioId, String nome);

    List<Categoria> findByUsuarioId(Long usuarioId);
    
    Optional<Categoria> findByNome(String nome);
    
    Page<Categoria> findByUsuarioId(Long usuarioId, Pageable pageable);
}
