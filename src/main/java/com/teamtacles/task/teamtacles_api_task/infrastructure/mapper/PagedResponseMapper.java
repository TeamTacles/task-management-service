package com.teamtacles.task.teamtacles_api_task.infrastructure.mapper;

import com.teamtacles.task.teamtacles_api_task.application.dto.response.PagedResponse;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PagedResponseMapper {
    private final ModelMapper modelMapper;

    public PagedResponseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // Método original, para conversões simples
    public <S, T> PagedResponse<T> toPagedResponse(Page<S> sourcePage, Class<T> targetClass) {
        List<T> mappedContent = sourcePage.getContent()
                .stream()
                .map(source -> modelMapper.map(source, targetClass))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                mappedContent,
                sourcePage.getNumber(),
                sourcePage.getSize(),
                sourcePage.getTotalElements(),
                sourcePage.getTotalPages(),
                sourcePage.isLast()
        );
    }

    // NOVO MÉTODO: Sobrecarga que aceita uma função de mapeamento customizada
    public <S, T> PagedResponse<T> toPagedResponse(Page<S> sourcePage, Function<S, T> converter) {
        List<T> mappedContent = sourcePage.getContent()
                .stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                mappedContent,
                sourcePage.getNumber(),
                sourcePage.getSize(),
                sourcePage.getTotalElements(),
                sourcePage.getTotalPages(),
                sourcePage.isLast()
        );
    }
}