package com.finanCerto.finanCertoBack.account;

import com.finanCerto.finanCertoBack.account.dtos.AccountRegistrationDto;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_conta", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nome"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "nome", nullable = false)
    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private AccountType type;

    @Column(name = "saldo_inicial", nullable = false)
    @NotNull
    private double initialBalance;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User user;

    public Account(AccountRegistrationDto dto, User user) {
        this.name = dto.name();
        this.initialBalance = dto.initialBalance();
        this.type = dto.type();
        this.user = user;
    }
}
