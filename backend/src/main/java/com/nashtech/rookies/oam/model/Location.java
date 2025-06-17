package com.nashtech.rookies.oam.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity(name = "locations")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    String code;

    @Column(nullable = false)
    String name;

}
