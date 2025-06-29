package com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagedResponse<T>{
    @Schema(description = "The list of elements for the current page.", type = "array")
	private List<T> content;

    @Schema(description = "The current page number (0-indexed).", example = "0")
    private int page;

    @Schema(description = "The number of elements in the current page.", example = "10")
    private int size;

    @Schema(description = "The total number of elements across all pages.", example = "100")
    private long totalElements;

    @Schema(description = "The total number of pages available.", example = "10")
    private int totalPages;

    @Schema(description = "Indicates if the current page is the last page.", example = "false")
    private boolean last;
}