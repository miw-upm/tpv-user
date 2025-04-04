package es.upm.api.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "betcaUser") // conflict with user table
public class User {
    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;
    @Column(unique = true, nullable = false)
    private String mobile;
    private String firstName;
    private String familyName;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String dni;
    private String address;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime registrationDate;
    private Boolean active;
}
