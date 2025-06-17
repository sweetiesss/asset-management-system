package com.nashtech.rookies.oam.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserAssignmentEditView {
    UUID id;
    String fullName;
}
