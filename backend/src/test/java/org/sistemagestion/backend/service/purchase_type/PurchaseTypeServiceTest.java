package org.sistemagestion.backend.service.purchase_type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeRequestDto;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PurchaseTypeMapper;
import org.sistemagestion.backend.model.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.PurchaseTypeRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseTypeService")
class PurchaseTypeServiceTest {

    @Mock
    private PurchaseTypeRepository repository;

    // El mapper es logica simple sin dependencias: se usa el real
    @Spy
    private PurchaseTypeMapper mapper = new PurchaseTypeMapper();

    @InjectMocks
    private PurchaseTypeService service;

    // ====== Helpers ======

    private PurchaseTypeRequestDto request(String name, Specie specie) {
        return PurchaseTypeRequestDto.builder()
                .name(name)
                .specie(specie)
                .build();
    }

    private PurchaseType type(Long id, String name, Specie specie, boolean active) {
        return PurchaseType.builder()
                .id(id)
                .name(name)
                .specie(specie)
                .isActive(active)
                .build();
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Debe guardar el tipo activo y con el nombre sin espacios")
        void createShouldSaveTrimmedActiveType() {

            // Arrange
            when(repository.existsByNameIgnoreCaseAndSpecieAndIsActiveTrue("Ternero",Specie.BOVINE))
                    .thenReturn(false);
            when(repository.save(any(PurchaseType.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            PurchaseTypeResponseDto result = service.create(request("  Ternero  ", Specie.BOVINE));

            // Assert
            assertThat(result.getName()).isEqualTo("Ternero");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Debe rechazar un nombre ya usado por un tipo activo de la misma especie")
        void createShouldRejectDuplicatedName() {

            // Arrange
            when(repository.existsByNameIgnoreCaseAndSpecieAndIsActiveTrue("Ternero", Specie.BOVINE))
                    .thenReturn(true);

            // Act
            Throwable thrown = catchThrowable(() -> service.create(request("Ternero", Specie.BOVINE)));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Debe cambiar el nombre del tipo")
        void updateShouldRenameType() {

            // Arrange
            PurchaseType existing = type(1L, "Ternero", Specie.BOVINE, true);

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.existsByNameIgnoreCaseAndSpecieAndIsActiveTrueAndIdNot("Novillo",Specie.BOVINE, 1L))
                    .thenReturn(false);
            when(repository.save(existing)).thenReturn(existing);

            // Act
            PurchaseTypeResponseDto result = service.update(1L, request("  Novillo  ", Specie.BOVINE));

            // Assert
            assertThat(result.getName()).isEqualTo("Novillo");
        }

        @Test
        @DisplayName("Debe rechazar el cambio de especie")
        void updateShouldRejectSpecieChange() {

            // Arrange
            when(repository.findById(1L)).thenReturn(Optional.of(type(1L, "Ternero", Specie.BOVINE, true)));

            // Act
            Throwable thrown = catchThrowable(() -> service.update(1L, request("Ternero",Specie.PORCINE)));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Debe fallar si el tipo no existe")
        void updateShouldFailWhenNotFound() {

            // Arrange
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Throwable thrown = catchThrowable(() -> service.update(99L, request("Ternero", Specie.BOVINE)));

            // Assert
            assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("activate()")
    class ActivateTests {

        @Test
        @DisplayName("Debe reactivar un tipo archivado")
        void activateShouldReactivateArchivedType() {

            // Arrange
            PurchaseType archived = type(1L,"Ternero", Specie.BOVINE, false);

            when(repository.findById(1L)).thenReturn(Optional.of(archived));

            // Act
            service.activate(1L);

            // Assert
            assertThat(archived.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Debe rechazar la reactivación si ya hay un tipo activo con ese nombre")
        void activateShouldRejectedReactivationWhenNameTaken() {

            // Arrange
            when(repository.findById(1L)).thenReturn(Optional.of(type(1L, "Ternero",Specie.BOVINE, false)));
            when(repository.existsByNameIgnoreCaseAndSpecieAndIsActiveTrueAndIdNot("Ternero", Specie.BOVINE, 1L))
                    .thenReturn(true);

            // Act
            Throwable thrown = catchThrowable(() -> service.activate(1L));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(repository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Debe archivar el tipo en lugar de borrarlo")
        void deleteShouldArchiveInsteadOfDelete() {

            // Arrange
            PurchaseType existing = type(1L, "Ternero", Specie.BOVINE, true);

            when(repository.findById(1L)).thenReturn(Optional.of(existing));

            // Act
            service.delete(1L);

            // Assert
            assertThat(existing.getIsActive()).isFalse();
            verify(repository).save(existing);
        }

    }

}