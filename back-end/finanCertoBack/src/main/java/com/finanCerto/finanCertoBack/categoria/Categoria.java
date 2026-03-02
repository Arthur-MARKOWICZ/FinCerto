package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_categoria",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nome"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    @NotBlank
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Tipo tipo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull
    private Usuario usuario;

    public Categoria(CategoriaCadastroDto dto) {
        this.nome=dto.nome();
        this.tipo = dto.tipo();

    }
}
