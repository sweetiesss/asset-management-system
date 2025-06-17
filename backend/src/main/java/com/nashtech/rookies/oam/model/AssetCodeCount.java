package com.nashtech.rookies.oam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity(name = "asset_code_count")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetCodeCount {
    @Id
    @EqualsAndHashCode.Include
    String id;

    @Column(nullable = false, columnDefinition = "int default 0")
    Integer lastValue;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    Long version;
}
