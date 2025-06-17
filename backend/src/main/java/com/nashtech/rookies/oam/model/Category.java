package com.nashtech.rookies.oam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity(name = "categories")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, unique = true)
    String name;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    @Size(min = 2, max = 2)
    String prefix;

    @Version
    @Column(columnDefinition = "bigint default 0")
    Long version;
}
