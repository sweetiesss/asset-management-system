package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.exception.CategoryEmptyException;
import com.nashtech.rookies.oam.model.AssetCodeCount;
import com.nashtech.rookies.oam.repository.AssetCodeCountRepository;
import com.nashtech.rookies.oam.service.AssetCodeGeneratorService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCodeGeneratorServiceImpl implements AssetCodeGeneratorService {
    private static final String ASSET_CODE_FORMAT = "%s%06d";
    private static final Integer ASSET_CODE_INCREMENT_BY = 1;

    private final AssetCodeCountRepository assetCodeCountRepository;

    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 100))
    @Override
    public String generateAssetCode(String categoryPrefix) {
        if(StringUtils.isBlank(categoryPrefix)) {
            log.error("Category prefix is null or empty");
            throw new CategoryEmptyException(ErrorCode.CATEGORY_EMPTY.getMessage());
        }

        AssetCodeCount sequence = assetCodeCountRepository.findByIdForUpdate(categoryPrefix)
                .orElseGet(() -> createNewAssetCodeCount(categoryPrefix));

        Integer lastValue = Optional.ofNullable(sequence.getLastValue()).orElse(0);
        Integer next = lastValue + ASSET_CODE_INCREMENT_BY;

        sequence.setLastValue(next);
        assetCodeCountRepository.save(sequence);

        return String.format(ASSET_CODE_FORMAT, categoryPrefix, next);
    }

    private AssetCodeCount createNewAssetCodeCount(String categoryPrefix) {
        AssetCodeCount newAssetCodeCount = AssetCodeCount.builder().id(categoryPrefix).lastValue(0).build();
        return assetCodeCountRepository.save(newAssetCodeCount);
    }
}
