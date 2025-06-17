package com.nashtech.rookies.oam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(unique = true, nullable = false, length = 10)
    @EqualsAndHashCode.Include
    String staffCode;

    @Column(unique = true, nullable = false, length = 50)
    String username;

    @Column(unique = true, nullable = false)
    @JsonIgnore
    String hashedPassword;

    @Column(nullable = false, length = 128)
    String firstName;

    @Column(nullable = false, length = 128)
    String lastName;

    @Column(nullable = false)
    LocalDate dateOfBirth;

    @Column(nullable = false)
    LocalDate joinedOn;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    Location location;

    @Enumerated(EnumType.STRING)
    UserStatus status;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    Long version;
}