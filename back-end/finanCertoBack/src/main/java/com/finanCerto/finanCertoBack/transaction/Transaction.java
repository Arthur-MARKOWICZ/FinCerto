package com.finanCerto.finanCertoBack.transaction;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.transaction.dto.TransactionRegistrationDto;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transacao")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor", nullable = false)
    private double amount;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "descricao")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User user;

    public Transaction(TransactionRegistrationDto dto, User user, Category category, Account account) {
        this.description = dto.description();
        this.amount = dto.amount();
        this.type = dto.type();
        this.user = user;
        this.date = dto.date();
        this.category = category;
        this.account = account;
    }
}
