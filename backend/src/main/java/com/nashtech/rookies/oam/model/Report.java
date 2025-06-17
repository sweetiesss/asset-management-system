package com.nashtech.rookies.oam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "report_view")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Report {
    @Id
    @Column(name = "id", nullable = false)
    int id;

    @Column(name = "category", nullable = false)
    String category;

    @Column(name = "total", nullable = false)
    long total;

    @Column(name = "assigned", nullable = false)
    long assigned;

    @Column(name = "available", nullable = false)
    long available;

    @Column(name = "not_available", nullable = false)
    long notAvailable;

    @Column(name = "waiting_for_recycling", nullable = false)
    long waitingForRecycling;

    @Column(name = "recycled", nullable = false)
    long recycled;
}
