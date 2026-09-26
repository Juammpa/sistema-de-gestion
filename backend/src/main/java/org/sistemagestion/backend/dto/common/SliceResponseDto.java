package org.sistemagestion.backend.dto.common;

import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.function.Function;

// Una pagina de resultados para scroll infinito (sin total, solo si hay mas)
public record SliceResponseDto<T>(
        List<T> content,
        int page,
        boolean hasNext
) {
    public static <E, T> SliceResponseDto<T> from(Slice<E> slice, Function<E, T> mapper) {
        return new SliceResponseDto<>(
                slice.getContent().stream().map(mapper).toList(),
                slice.getNumber(),
                slice.hasNext());
    }
}
