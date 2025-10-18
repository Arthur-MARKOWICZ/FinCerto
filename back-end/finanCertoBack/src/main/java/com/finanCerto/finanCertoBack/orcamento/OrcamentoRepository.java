package com.finanCerto.finanCertoBack.orcamento;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoRepository extends JpaRepository<Orcamento,Long> {
    boolean existsByUsuarioIdAndNome(Long usuarioId, String nome);

    Orcamento findByUsuarioIdAndNome(Long usuarioId,String nome);

    Orcamento findByCategoria(Categoria categoria);
}
