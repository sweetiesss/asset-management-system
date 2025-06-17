package com.nashtech.rookies.oam.projection;

import com.nashtech.rookies.oam.model.Assignment;

import java.time.LocalDate;

public interface AssignmentWithReturnDate {
    Assignment getAssignment();

    LocalDate getReturnedDate();
}
