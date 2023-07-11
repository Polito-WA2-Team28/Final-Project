package com.final_project.ticketing.dto

import com.final_project.ticketing.model.TicketStateEvolution
import com.final_project.ticketing.util.TicketState
import java.util.*

data class TicketStateEvolutionDTO(
    val state: TicketState,
    val timestamp: Date
) {}


fun TicketStateEvolution.toDTO(): TicketStateEvolutionDTO {
    return TicketStateEvolutionDTO(state, timestamp)
}