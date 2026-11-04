package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.account.DeleteAccountRequestDto;
import org.sistemagestion.backend.service.account.IAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    // Elimina la cuenta de quien hace el pedido y todos sus datos.
    @PostMapping("/deletion")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequestDto request, @AuthenticationPrincipal Jwt jwt) {

        accountService.deleteAccount(jwt.getSubject(), request.confirmation());
        return ResponseEntity.noContent().build();
    }

}
