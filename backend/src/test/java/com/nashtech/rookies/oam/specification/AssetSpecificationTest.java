package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.config.AuditorAwareProvider;
import com.nashtech.rookies.oam.config.AuditorAwareTestConfig;
import com.nashtech.rookies.oam.config.JpaConfig;
import com.nashtech.rookies.oam.config.SpringContext;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Category;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.repository.AssetRepository;
import com.nashtech.rookies.oam.repository.CategoryRepository;
import com.nashtech.rookies.oam.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SpringContext.class, AuditorAwareTestConfig.class, AuditorAwareProvider.class})
class AssetSpecificationTest {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CategoryRepository categoryRepository; // Add this

    @Autowired
    private LocationRepository locationRepository; // Add this

    private Location testLocation;

    @BeforeEach
    void setUp() {
        // Save Location first
        testLocation = Location.builder()
                .code("LOC001")   // <-- add a valid code here, non-null
                .name("Test Location")
                .build();
        testLocation = locationRepository.save(testLocation);

        // Save Categories first
        Category electronics = Category.builder()
                .name("Electronics")
                .prefix("EL")
                .build();
        electronics = categoryRepository.save(electronics);

        Category furniture = Category.builder()
                .name("Furniture")
                .prefix("FU")
                .build();
        furniture = categoryRepository.save(furniture);

        // Now build Assets with saved categories and location (with IDs and versions initialized)
        Asset asset1 = Asset.builder()
                .code("AST001")
                .name("Laptop")
                .specification("Dell XPS 13")
                .installedDate(LocalDate.now())
                .state(AssetState.AVAILABLE)
                .category(electronics)
                .location(testLocation)
                .version(1L)  // optional, usually managed by JPA
                .build();

        Asset asset2 = Asset.builder()
                .code("AST002")
                .name("Desk")
                .specification("Wooden desk")
                .installedDate(LocalDate.now())
                .state(AssetState.NOT_AVAILABLE)
                .category(furniture)
                .location(testLocation)
                .version(1L)
                .build();

        assetRepository.saveAll(List.of(asset1, asset2));
    }

    @Test
    void searchByCodeOrName_ShouldReturnMatchingAssets() {
        Specification<Asset> spec = AssetSpecification.searchByCodeOrName("desk");
        List<Asset> results = assetRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Desk");
    }

    @Test
    void filterByCategories_ShouldReturnMatchingAssets() {
        Specification<Asset> spec = AssetSpecification.filterByCategories(List.of("Electronics"));
        List<Asset> results = assetRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCategory().getName()).isEqualTo("Electronics");
    }

    @Test
    void filterByStates_ShouldReturnMatchingAssets() {
        Specification<Asset> spec = AssetSpecification.filterByStates(List.of("NOT_AVAILABLE"));
        List<Asset> results = assetRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getState()).isEqualTo(AssetState.NOT_AVAILABLE);
    }

    @Test
    void inLocation_ShouldReturnMatchingAssets() {
        Specification<Asset> spec = AssetSpecification.inLocation(testLocation);
        List<Asset> results = assetRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(asset -> asset.getLocation().getId().equals(testLocation.getId()));
    }

    @Test
    void build_ShouldReturnAssetsMatchingAllCriteria() {
        Specification<Asset> spec = AssetSpecification.build("Laptop", List.of("Electronics"), List.of("AVAILABLE"), testLocation);
        List<Asset> results = assetRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Laptop");
    }
}
