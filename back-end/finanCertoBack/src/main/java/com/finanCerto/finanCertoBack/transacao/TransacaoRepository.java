package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.conta.Conta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao,Long> {

    Page<Transacao> findByContaId(Long contaId, Pageable pageable);
    
    Page<Transacao> findByUsuarioId(Long usuarioId, Pageable pageable);
    
    Page<Transacao> findByCategoriaId(Long categoriaId, Pageable pageable);
}
