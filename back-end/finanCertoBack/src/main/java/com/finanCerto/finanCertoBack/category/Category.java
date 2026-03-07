package com.finanCerto.finanCertoBack.category;

import com.finanCerto.finanCertoBack.category.dto.CategoryRegistrationDto;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_categoria", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nome"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "nome", nullable = false)
    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @NotNull
    private CategoryType type;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull
    private User user;

    public Category(CategoryRegistrationDto dto) {
        this.name = dto.name();
        this.type = dto.type();
    }
}
