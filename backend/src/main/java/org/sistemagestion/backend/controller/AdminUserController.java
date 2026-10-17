package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.user.UserCreateRequestDto;
import org.sistemagestion.backend.dto.user.UserResponseDto;
import org.sistemagestion.backend.dto.user.UserRoleRequestDto;
import org.sistemagestion.backend.service.user.IUserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final IUserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<SliceResponseDto<UserResponseDto>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size)
    {
        return ResponseEntity.ok(userAdminService.getUsers(search, enabled, page, size));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.createUser(request));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDto> changeRole(@PathVariable UUID id, @Valid @RequestBody UserRoleRequestDto request, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(userAdminService.changeRole(id.toString(), request.role(), jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {

        userAdminService.disableUser(id.toString(), jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable UUID id) {

        userAdminService.activateUser(id.toString());
        return ResponseEntity.noContent().build();
    }

}
