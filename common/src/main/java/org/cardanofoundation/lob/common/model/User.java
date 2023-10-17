package org.cardanofoundation.lob.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Name can't be empty")
    private String name;

    @NotEmpty(message = "Email can't be empty")
    @Email(message = "Email not valid")
    private String email;

    @NotEmpty
    private String password;

    private Boolean enabled;

    @NotEmpty
    private String[] role;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private Account account;
}
