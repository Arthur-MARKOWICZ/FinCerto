package com.finanCerto.finanCertoBack.orcamento;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.orcamento.dto.OrcamentoCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_orcamento",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nome"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double valorLimite;
    private String nome;
    private LocalDate prazo;
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Orcamento(OrcamentoCadastroDto dto, Categoria categoria, Usuario usuario) {
        this.valorLimite =dto.valorLimite();
        this.nome = dto.nome();
        this.prazo = dto.prazo();
        this.categoria =categoria;
        this.usuario =usuario;
    }
}
