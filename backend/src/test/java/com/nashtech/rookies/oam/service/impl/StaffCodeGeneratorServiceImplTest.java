package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.exception.InternalErrorException;
import com.nashtech.rookies.oam.model.StaffCodeCount;
import com.nashtech.rookies.oam.repository.StaffCodeCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaffCodeGeneratorServiceImplTest {

    @Mock
    private StaffCodeCountRepository staffCodeCountRepository;

    @InjectMocks
    private StaffCodeGeneratorServiceImpl staffCodeGeneratorService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateStaffCode_shouldReturnFormattedCode_whenSequenceExists() {
        StaffCodeCount sequence = new StaffCodeCount();
        sequence.setLastValue(5);
        when(staffCodeCountRepository.findByIdForUpdate("SD")).thenReturn(Optional.of(sequence));
        when(staffCodeCountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String staffCode = staffCodeGeneratorService.generateStaffCode();

        assertEquals("SD0006", staffCode);
        verify(staffCodeCountRepository).findByIdForUpdate("SD");
        verify(staffCodeCountRepository).save(sequence);
        assertEquals(6, sequence.getLastValue());
    }

    @Test
    void generateStaffCode_shouldThrowInternalErrorException_whenSequenceNotFound() {
        when(staffCodeCountRepository.findByIdForUpdate("SD")).thenReturn(Optional.empty());

        InternalErrorException ex = assertThrows(InternalErrorException.class,
                () -> staffCodeGeneratorService.generateStaffCode());

        assertTrue(ex.getMessage().contains(ErrorCode.STAFF_CODE_COUNT_NOT_FOUND.getMessage()));
        verify(staffCodeCountRepository).findByIdForUpdate("SD");
        verify(staffCodeCountRepository, never()).save(any());
    }

    @Test
    void generateStaffCode_shouldHandleNullLastValueAsZero() {
        StaffCodeCount sequence = new StaffCodeCount();
        sequence.setLastValue(null);
        when(staffCodeCountRepository.findByIdForUpdate("SD")).thenReturn(Optional.of(sequence));
        when(staffCodeCountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String staffCode = staffCodeGeneratorService.generateStaffCode();

        assertEquals("SD0001", staffCode);
        assertEquals(1, sequence.getLastValue());
    }
}
