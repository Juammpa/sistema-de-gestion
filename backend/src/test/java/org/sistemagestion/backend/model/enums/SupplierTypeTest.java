package org.sistemagestion.backend.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SupplierType")
class SupplierTypeTest {

    @Nested
    @DisplayName("canSell()")
    class CanSellTests {

        @Test
        @DisplayName("Debe permitir solo bovinos a un proveedor de bovinos")
        void canSellShouldSellOnlyBovine() {

            // Arrange
            SupplierType type = SupplierType.BOVINE;

            // Act
            boolean sellsBovine = type.canSell(Specie.BOVINE);
            boolean sellsPorcine = type.canSell(Specie.PORCINE);

            // Assert
            assertThat(sellsBovine).isTrue();
            assertThat(sellsPorcine).isFalse();
        }

        @Test
        @DisplayName("Debe permitir ambas especies a un proveedor mixto")
        void canSellShouldSellBothSpecies() {

            // Arrange
            SupplierType type = SupplierType.BOTH;

            // Act + Assert
            assertThat(type.canSell(Specie.BOVINE)).isTrue();
            assertThat(type.canSell(Specie.PORCINE)).isTrue();
        }

    }

    @Nested
    @DisplayName("incompatibleSpecies()")
    class IncompatibleSpeciesTests {

        @Test
        @DisplayName("Debe devolver las especies que el proveedor no vende")
        void incompatibleSpeciesShouldReturnSpeciesNotSold() {

            // Act
            var bovineIncompatible = SupplierType.BOVINE.incompatibleSpecies();
            var bothIncompatible = SupplierType.BOTH.incompatibleSpecies();

            // Assert
            assertThat(bovineIncompatible).containsExactly(Specie.PORCINE);
            assertThat(bothIncompatible).isEmpty();
        }

    }

}