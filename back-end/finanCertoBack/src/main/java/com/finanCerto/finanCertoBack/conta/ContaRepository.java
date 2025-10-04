package com.finanCerto.finanCertoBack.conta;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta,Long> {
    boolean existsByUsuarioIdAndNome(Long usuarioId, String nome);
}
