package com.finanCerto.finanCertoBack.user;

import com.finanCerto.finanCertoBack.user.dtos.UserRegistrationDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "nome", nullable = false)
    @NotBlank
    private String name;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(name = "senha", nullable = false)
    @NotBlank
    private String password;

    public User(UserRegistrationDto dto) {
        this.name = dto.name();
        this.email = dto.email();
        this.password = dto.password();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
