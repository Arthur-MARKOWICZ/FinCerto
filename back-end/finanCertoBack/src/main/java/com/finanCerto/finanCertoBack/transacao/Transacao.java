package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.transacao.dto.TransacaoCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.IdGeneratorType;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transacao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private double valor;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    private String descricao;
    @Enumerated(EnumType.STRING)
    private Tipos tipo;
    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Transacao(TransacaoCadastroDto dto, Usuario usuario, Categoria categoria, Conta conta) {
        this.descricao = dto.descricao();
        this.valor = dto.valor();
        this.tipo = dto.tipo();
        this.usuario = usuario;
        this.date  = dto.data();
        this.categoria = categoria;
        this.conta = conta;

    }

}
