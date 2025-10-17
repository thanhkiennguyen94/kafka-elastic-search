package com.example.commonservice.util;

import com.example.commonservice.entity.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import com.example.commonservice.entity.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public class PageMapperUtil {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static <E, D> PageResponse<D> toPageResponse(Page<E> page, Class<D> dtoClass) {
        List<D> dtoList = page.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, dtoClass))
                .toList();

        return PageResponse.<D>builder()
                .content(dtoList)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    public static <E, D> PageResponse<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        List<D> dtoList = page.getContent().parallelStream()
                .map(mapper)
                .toList();

        return PageResponse.<D>builder()
                .content(dtoList)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    public static <E, D> ApiResponse<PageResponse<D>> toApiResponse(Page<E> page, Class<D> dtoClass) {
        PageResponse<D> pageResponse = toPageResponse(page, dtoClass);
        return ApiResponse.success(pageResponse);
    }

    public static <E, D> ApiResponse<PageResponse<D>> toApiResponse(Page<E> page, Function<E, D> mapper) {
        PageResponse<D> pageResponse = toPageResponse(page, mapper);
        return ApiResponse.success(pageResponse);
    }
}
