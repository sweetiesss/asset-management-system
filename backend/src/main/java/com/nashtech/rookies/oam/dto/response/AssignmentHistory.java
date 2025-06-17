package com.nashtech.rookies.oam.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AssignmentHistory {
    UUID id;
    LocalDate assignedDate;
    String assignedTo;
    String assignedBy;
    LocalDate returnedDate;
}
