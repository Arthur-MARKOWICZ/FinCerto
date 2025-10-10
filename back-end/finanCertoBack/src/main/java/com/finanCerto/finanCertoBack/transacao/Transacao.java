package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.conta.Conta;
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
}
