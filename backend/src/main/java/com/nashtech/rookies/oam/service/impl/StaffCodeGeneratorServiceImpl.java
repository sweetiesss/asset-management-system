package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.exception.InternalErrorException;
import com.nashtech.rookies.oam.model.StaffCodeCount;
import com.nashtech.rookies.oam.repository.StaffCodeCountRepository;
import com.nashtech.rookies.oam.service.StaffCodeGeneratorService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffCodeGeneratorServiceImpl implements StaffCodeGeneratorService {

    private static final String STAFF_CODE = "SD";
    private static final String STAFF_CODE_FORMAT = "%s%04d";
    private static final Integer STAFF_CODE_INCREMENT_BY = 1;

    private final StaffCodeCountRepository staffCodeCountRepository;

    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 100))
    @Override
    public String generateStaffCode() {
        StaffCodeCount sequence = staffCodeCountRepository.findByIdForUpdate(STAFF_CODE)
                .orElseThrow(() -> {
                    log.debug("Staff code count not found, checking database for initialization");
                    return new InternalErrorException(ErrorCode.STAFF_CODE_COUNT_NOT_FOUND.getMessage(), null);
                });

        Integer lastValue = sequence.getLastValue();
        if (lastValue == null) {
            lastValue = 0;
        }

        Integer next = lastValue + STAFF_CODE_INCREMENT_BY;

        sequence.setLastValue(next);
        staffCodeCountRepository.save(sequence);

        return String.format(STAFF_CODE_FORMAT, STAFF_CODE, next);
    }
}