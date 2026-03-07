package com.finanCerto.finanCertoBack.budget;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.budget.dto.BudgetRegistrationDto;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tb_orcamento", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nome"}),
        @UniqueConstraint(columnNames = {"categoria_id", "nome"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "valor_limite")
    private double limitValue;

    @Column(name = "valor_atual")
    private double currentValue;

    @Column(name = "nome")
    private String name;

    @Column(name = "prazo")
    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User user;

    public Budget(BudgetRegistrationDto dto, Category category, User user) {
        this.limitValue = dto.limitValue();
        this.name = dto.name();
        this.currentValue = dto.initialValue();
        this.deadline = dto.deadline();
        this.category = category;
        this.user = user;
    }
}
