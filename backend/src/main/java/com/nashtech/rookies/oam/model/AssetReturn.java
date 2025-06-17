package com.nashtech.rookies.oam.model;

import com.nashtech.rookies.oam.model.enums.ReturnState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "asset_returns")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssetReturn extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    ReturnState state;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", nullable = false)
    Assignment assignment;

    @Column(columnDefinition = "bigint default 0")
    Long version;
}
