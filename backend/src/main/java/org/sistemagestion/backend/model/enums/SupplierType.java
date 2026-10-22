package org.sistemagestion.backend.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
public enum SupplierType {

    BOVINE("bovinos", EnumSet.of(Specie.BOVINE)), // Bovinos
    PORCINE("porcinos", EnumSet.of(Specie.PORCINE)),    // Porcinos
    BOTH("bovinos y porcinos", EnumSet.allOf(Specie.class));

    private final String label;
    private final Set<Specie> species;

    SupplierType(String label, Set<Specie> species) {
        this.label = label;
        this.species = species;
    }

    // Verifica si el proveedor puede vender esa especie
    public boolean canSell(Specie specie) {
        return species.contains(specie);
    }

    // Especies que el proveedor NO puede vender
    public List<Specie> incompatibleSpecies() {
        return Arrays.stream(Specie.values())
                .filter(specie -> !canSell(specie))
                .toList();
    }
}
