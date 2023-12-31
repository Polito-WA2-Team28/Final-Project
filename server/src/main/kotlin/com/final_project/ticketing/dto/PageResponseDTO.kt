package com.final_project.ticketing.dto

import com.final_project.server.dto.ExpertDTO
import com.final_project.server.dto.ProductDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

class PageResponseDTO<T>(
    val content: List<T>,
    val pageSize: Int,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val sort: Sort?
) {

    /* empty constructor */
    constructor() : this(
        content = emptyList(),
        pageSize = 0,
        currentPage = 0,
        totalPages = 0,
        totalElements = 0,
        sort = null
    )

    companion object {
        fun <T> Page<T>.toDTO(): PageResponseDTO<T> {
            return PageResponseDTO(
                content = this.content,
                pageSize = this.pageable.pageSize,
                currentPage = this.number + 1,
                totalPages = this.totalPages,
                totalElements = this.totalElements,
                sort = this.sort
            )
        }
    }
}


inline fun <reified T> PageResponseDTO<T>.computePageSize(): Int {
    return when (T::class.java) {
        MessageDTO::class.java -> 30
        TicketDTO::class.java -> 5
        TicketManagerDTO::class.java -> 5
        ExpertDTO::class.java -> 6
        ProductDTO::class.java -> 6
        /* add here if needed */
        else -> throw IllegalArgumentException("Unsupported class: ${T::class.java}")
    }
}
