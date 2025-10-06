package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_categoria")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    @Column(nullable = false)
    @NotBlank
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Tipo tipo;
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
