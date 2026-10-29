package org.sistemagestion.backend.service.custom_payment_method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodRequestDto;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.CustomPaymentMethodMapper;
import org.sistemagestion.backend.model.CustomPaymentMethod;
import org.sistemagestion.backend.repository.CustomPaymentMethodRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomPaymentMethodService")
class CustomPaymentMethodServiceTest {

    @Mock
    private CustomPaymentMethodRepository repository;

    // El mapper es logica simple sin dependencias: se usa el real
    @Spy
    private CustomPaymentMethodMapper mapper = new CustomPaymentMethodMapper();

    @InjectMocks
    private CustomPaymentMethodService service;

    // ====== Helpers ======

    private CustomPaymentMethodRequestDto request(String name) {
        return CustomPaymentMethodRequestDto.builder()
                .name(name)
                .build();
    }

    private CustomPaymentMethod method(Long id, String name, boolean active) {
        return CustomPaymentMethod.builder()
                .id(id)
                .name(name)
                .isActive(active)
                .build();
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Debe guardar el metodo activo y con el nombre sin espacios")
        void createShouldSaveTrimmedActiveMethod() {

            // Arrange
            when(repository.existsByNameIgnoreCaseAndIsActiveTrue("Mercado Pago")).thenReturn(false);
            when(repository.save(any(CustomPaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CustomPaymentMethodResponseDto result = service.create(request("  Mercado Pago  "));

            // Assert
            assertThat(result.getName()).isEqualTo("Mercado Pago");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Debe rechazar un nombre ya usado por un metodo activo")
        void createShouldRejectDuplicatedName() {

            // Arrange
            when(repository.existsByNameIgnoreCaseAndIsActiveTrue("Mercado Pago")).thenReturn(true);

            // Act
            Throwable thrown = catchThrowable(() -> service.create(request("Mercado Pago")));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(repository, never()).save(any());
        }

        // Los nombres de fabrica se rechazan sin consultar la base, sin importar mayusculas ni espacios
        @ParameterizedTest
        @ValueSource(strings = {"Efectivo", "  TRANSFERENCIA ", "cheque", "Transferencia a proveedor", "Cheque Rechazado"})
        @DisplayName("Debe rechazar los nombres de los metodos de fabrica")
        void createShouldRejectBuiltInNames(String name) {

            // Act
            Throwable thrown = catchThrowable(() -> service.create(request(name)));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Debe cambiar el nombre del metodo")
        void updateShouldRenameMethod() {

            // Arrange
            CustomPaymentMethod existing = method(1L, "Mercado Pago", true);

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot("Ualá", 1L)).thenReturn(false);
            when(repository.save(any(CustomPaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CustomPaymentMethodResponseDto result = service.update(1L, request("  Ualá "));

            // Assert
            assertThat(result.getName()).isEqualTo("Ualá");
        }

        @Test
        @DisplayName("Debe rechazar un nombre que ya usa otro metodo activo")
        void updateShouldRejectNameUsedByAnotherMethod() {

            // Arrange
            when(repository.findById(1L)).thenReturn(Optional.of(method(1L, "Mercado Pago", true)));
            when(repository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot("Ualá", 1L)).thenReturn(true);

            // Act
            Throwable thrown = catchThrowable(() -> service.update(1L, request("Ualá")));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar un metodo que no existe")
        void updateShouldRejectUnknownMethod() {

            // Arrange
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Throwable thrown = catchThrowable(() -> service.update(99L, request("Ualá")));

            // Assert
            assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Debe desactivar el metodo sin borrarlo")
        void deleteShouldOnlyDeactivateMethod() {

            // Arrange
            CustomPaymentMethod existing = method(1L, "Mercado Pago", true);
            when(repository.findById(1L)).thenReturn(Optional.of(existing));

            // Act
            service.delete(1L);

            // Assert: los pagos viejos conservan su metodo
            assertThat(existing.getIsActive()).isFalse();
            verify(repository).save(existing);
            verify(repository, never()).delete(any());
            verify(repository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("activate()")
    class ActivateTests {

        @Test
        @DisplayName("Debe reactivar el metodo si su nombre sigue libre")
        void activateShouldReactivateMethod() {

            // Arrange
            CustomPaymentMethod existing = method(1L, "Mercado Pago", false);
            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot("Mercado Pago", 1L)).thenReturn(false);

            // Act
            service.activate(1L);

            // Assert
            assertThat(existing.getIsActive()).isTrue();
            verify(repository).save(existing);
        }

        @Test
        @DisplayName("Debe rechazar reactivar si ya hay otro metodo activo con ese nombre")
        void activateShouldRejectWhenNameWasTakenMeanwhile() {

            // Arrange
            CustomPaymentMethod existing = method(1L, "Mercado Pago", false);
            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot("Mercado Pago", 1L)).thenReturn(true);

            // Act
            Throwable thrown = catchThrowable(() -> service.activate(1L));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            assertThat(existing.getIsActive()).isFalse();
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("existsByName()")
    class ExistsByNameTests {

        @Test
        @DisplayName("Sin metodo excluido, consulta solo por nombre")
        void existsShouldQueryByNameOnly() {

            // Arrange
            when(repository.existsByNameIgnoreCaseAndIsActiveTrue("Mercado Pago")).thenReturn(true);

            // Act + Assert
            assertThat(service.existsByName(" Mercado Pago ", null)).isTrue();
        }

        @Test
        @DisplayName("Con metodo excluido, lo ignora al buscar")
        void existsShouldIgnoreTheExcludedMethod() {

            // Arrange
            when(repository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot("Mercado Pago", 1L)).thenReturn(false);

            // Act + Assert
            assertThat(service.existsByName("Mercado Pago", 1L)).isFalse();
        }

        @Test
        @DisplayName("Un nombre de fabrica cuenta como existente aunque se excluya un metodo")
        void existsShouldTreatBuiltInNamesAsTaken() {

            // Act + Assert
            assertThat(service.existsByName("Efectivo", 1L)).isTrue();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Devuelve activos y archivados")
        void getAllShouldReturnActiveAndArchivedMethods() {

            // Arrange
            when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(
                    method(1L, "Mercado Pago", true),
                    method(2L, "Ualá", false)));

            // Act
            List<CustomPaymentMethodResponseDto> result = service.getAll();

            // Assert
            assertThat(result).extracting(CustomPaymentMethodResponseDto::getIsActive)
                    .containsExactly(true, false);
        }
    }
}
