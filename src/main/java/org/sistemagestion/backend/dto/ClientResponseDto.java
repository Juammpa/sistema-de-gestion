package org.sistemagestion.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientResponseDto {

    private Long id;
    private String name;
    private String lastName;
    private String phoneNumber;
}
