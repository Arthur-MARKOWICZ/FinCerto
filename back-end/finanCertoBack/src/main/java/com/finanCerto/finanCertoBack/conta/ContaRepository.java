package com.finanCerto.finanCertoBack.conta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta,Long> {
    boolean existsByUsuarioIdAndNome(Long usuarioId, String nome);
    @Query("SELECT c FROM Conta c WHERE c.usuario.id = :usuarioId AND c.nome = :nome")
    Optional<Conta> findByNameAndUsuarioID(Long usuarioId,String nome);
}
