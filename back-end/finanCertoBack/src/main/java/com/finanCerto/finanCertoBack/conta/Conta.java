package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_conta",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nome"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    @NotBlank
    private String nome;
    @Enumerated(EnumType.STRING)
    private Tipos tipo;
    @Column(nullable = false)
    @NotNull
    private double saldoInicial;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Conta(ContaCadastroDto dto, Usuario usuario) {
        this.nome = dto.nome();
        this.saldoInicial = dto.saldoInicial();
        this.tipo = dto.tipos();
        this.usuario = usuario;
    }
}
