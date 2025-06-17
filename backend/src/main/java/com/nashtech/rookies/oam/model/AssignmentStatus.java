package com.nashtech.rookies.oam.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity(name = "assignment_statuses")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    String name;
}
