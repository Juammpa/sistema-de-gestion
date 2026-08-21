package org.sistemagestion.backend.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClientResponseDto {

    private Long id;
    private String name;
    private String lastName;
    private String phoneNumber;
}
