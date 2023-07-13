package com.final_project.ticketing.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

class TicketSurveyDTO(
    @field:NotBlank
    @field:NotNull
    @field:Size(max = 2000)
    val survey: String
) {}