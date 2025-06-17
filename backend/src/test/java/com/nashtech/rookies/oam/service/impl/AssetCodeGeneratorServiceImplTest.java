package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.exception.CategoryEmptyException;
import com.nashtech.rookies.oam.model.AssetCodeCount;
import com.nashtech.rookies.oam.repository.AssetCodeCountRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetCodeGeneratorServiceImplTest {

    @Mock
    private AssetCodeCountRepository assetCodeCountRepository;

    @InjectMocks
    private AssetCodeGeneratorServiceImpl assetCodeGeneratorService;

    private AssetCodeCount existingSequence;

    @BeforeEach
    void setUp() {
        existingSequence = AssetCodeCount.builder()
                .id("LP")
                .lastValue(1)
                .build();
    }

    @Test
    void generateAssetCode_WithValidCategoryPrefixAndExistingSequence_ShouldGenerateCodeSuccessfully() {
        when(assetCodeCountRepository.findByIdForUpdate("LP")).thenReturn(Optional.of(existingSequence));
        when(assetCodeCountRepository.save(any(AssetCodeCount.class))).thenReturn(existingSequence);

        String result = assetCodeGeneratorService.generateAssetCode("LP");

        assertEquals("LP000002", result);
        verify(assetCodeCountRepository).findByIdForUpdate("LP");
        verify(assetCodeCountRepository).save(existingSequence);
        assertEquals(2, existingSequence.getLastValue());
    }

    @Test
    void generateAssetCode_WithValidCategoryPrefixAndNoSequence_ShouldCreateNewSequenceAndGenerateCode() {
        when(assetCodeCountRepository.findByIdForUpdate("MON")).thenReturn(Optional.empty());
        AssetCodeCount newSequence = AssetCodeCount.builder().id("MON").lastValue(0).build();
        when(assetCodeCountRepository.save(any(AssetCodeCount.class))).thenReturn(newSequence);

        String result = assetCodeGeneratorService.generateAssetCode("MON");

        assertEquals("MON000001", result);
        verify(assetCodeCountRepository).findByIdForUpdate("MON");
        verify(assetCodeCountRepository, times(2)).save(any(AssetCodeCount.class));
        assertEquals(1, newSequence.getLastValue());
    }

    @Test
    void generateAssetCode_WithNullCategoryPrefix_ShouldThrowCategoryEmptyException() {
        CategoryEmptyException exception = assertThrows(CategoryEmptyException.class,
                () -> assetCodeGeneratorService.generateAssetCode(null));

        assertEquals(ErrorCode.CATEGORY_EMPTY.getMessage(), exception.getMessage());
        verify(assetCodeCountRepository, never()).findByIdForUpdate(anyString());
        verify(assetCodeCountRepository, never()).save(any(AssetCodeCount.class));
    }

    @Test
    void generateAssetCode_WithEmptyCategoryPrefix_ShouldThrowCategoryEmptyException() {
        CategoryEmptyException exception = assertThrows(CategoryEmptyException.class,
                () -> assetCodeGeneratorService.generateAssetCode(""));

        assertEquals(ErrorCode.CATEGORY_EMPTY.getMessage(), exception.getMessage());
        verify(assetCodeCountRepository, never()).findByIdForUpdate(anyString());
        verify(assetCodeCountRepository, never()).save(any(AssetCodeCount.class));
    }

    @Test
    void generateAssetCode_WithBlankCategoryPrefix_ShouldThrowCategoryEmptyException() {
        CategoryEmptyException exception = assertThrows(CategoryEmptyException.class,
                () -> assetCodeGeneratorService.generateAssetCode("   "));

        assertEquals(ErrorCode.CATEGORY_EMPTY.getMessage(), exception.getMessage());
        verify(assetCodeCountRepository, never()).findByIdForUpdate(anyString());
        verify(assetCodeCountRepository, never()).save(any(AssetCodeCount.class));
    }

    @Test
    void generateAssetCode_WithNullLastValueInSequence_ShouldHandleNullAndGenerateCode() {
        AssetCodeCount sequenceWithNull = AssetCodeCount.builder()
                .id("LP")
                .lastValue(null)
                .build();
        when(assetCodeCountRepository.findByIdForUpdate("LP")).thenReturn(Optional.of(sequenceWithNull));
        when(assetCodeCountRepository.save(any(AssetCodeCount.class))).thenReturn(sequenceWithNull);

        String result = assetCodeGeneratorService.generateAssetCode("LP");

        assertEquals("LP000001", result);
        verify(assetCodeCountRepository).findByIdForUpdate("LP");
        verify(assetCodeCountRepository).save(sequenceWithNull);
        assertEquals(1, sequenceWithNull.getLastValue());
    }

    @Test
    void generateAssetCode_WithOptimisticLockException_ShouldThrowException() {
        when(assetCodeCountRepository.findByIdForUpdate("LP"))
                .thenThrow(new OptimisticLockException("Lock error"));

        OptimisticLockException exception = assertThrows(OptimisticLockException.class,
                () -> assetCodeGeneratorService.generateAssetCode("LP"));

        assertEquals("Lock error", exception.getMessage());
        verify(assetCodeCountRepository, times(1)).findByIdForUpdate("LP");
        verify(assetCodeCountRepository, never()).save(any(AssetCodeCount.class));
    }

    @Test
    void generateAssetCode_WithLargeLastValue_ShouldFormatCodeCorrectly() {
        AssetCodeCount largeSequence = AssetCodeCount.builder()
                .id("LP")
                .lastValue(999999)
                .build();
        when(assetCodeCountRepository.findByIdForUpdate("LP")).thenReturn(Optional.of(largeSequence));
        when(assetCodeCountRepository.save(any(AssetCodeCount.class))).thenReturn(largeSequence);

        String result = assetCodeGeneratorService.generateAssetCode("LP");

        assertEquals("LP1000000", result);
        verify(assetCodeCountRepository).findByIdForUpdate("LP");
        verify(assetCodeCountRepository).save(largeSequence);
        assertEquals(1000000, largeSequence.getLastValue());
    }

    @Test
    void generateAssetCode_WithRepositorySaveFailure_ShouldThrowException() {
        when(assetCodeCountRepository.findByIdForUpdate("LP")).thenReturn(Optional.of(existingSequence));
        when(assetCodeCountRepository.save(any(AssetCodeCount.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assetCodeGeneratorService.generateAssetCode("LP"));

        assertEquals("Database error", exception.getMessage());
        verify(assetCodeCountRepository).findByIdForUpdate("LP");
        verify(assetCodeCountRepository).save(any(AssetCodeCount.class));
    }
}