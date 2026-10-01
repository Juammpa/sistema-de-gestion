package org.sistemagestion.backend.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseType {
    HALF_PIG(Specie.PORCINE),           // MEDIA CERDO
    VEAL_UP_TO_110KG(Specie.BOVINE),   // TERNERO HASTA 110KG
    NT_111_120KG(Specie.BOVINE),       // NT 111-120KG
    NT_121_130KG(Specie.BOVINE),       // NT 121-130KG
    STEER_OVER_130KG(Specie.BOVINE);    // NOVILLO +130KG

    private final Specie specie;
}
