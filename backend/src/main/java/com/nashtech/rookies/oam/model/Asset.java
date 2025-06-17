package com.nashtech.rookies.oam.model;

import com.nashtech.rookies.oam.model.enums.AssetState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "assets")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Asset extends AuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false, unique = true, length = 8)
    @EqualsAndHashCode.Include
    String code;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, length = 2000)
    String specification;

    @Column(nullable = false)
    LocalDate installedDate;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    AssetState state = AssetState.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    Location location;

    @Version
    @Column(columnDefinition = "bigint default 0")
    Long version;
}
