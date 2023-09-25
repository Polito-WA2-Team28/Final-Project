package com.final_project.ticketing.dto

import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.model.TicketStateEvolution
import com.final_project.ticketing.util.TicketState
import java.util.*

data class TicketManagerDTO(
    val ticketId: Long?,
    var ticketState: TicketState,
    val description: String,
    val serialNumber: UUID,
    val customerId: UUID?,
    var expertId: UUID?,
    var expertEmail: String?,
    val creationDate: Date,
    val lastModified: Date,
    val ticketStateLifecycle: List<TicketStateEvolutionDTO>,
    val survey:String?
) {

    fun assignExpert(expertId: UUID?) {
        this.expertId = expertId
    }

    fun relieveExpert() {
        this.expertId = null
    }

    fun changeState(newState: TicketState) {
        this.ticketState = newState
    }
}


fun TicketDTO.toManagerDTO(ticketStateLifecycle: List<TicketStateEvolutionDTO>?) : TicketManagerDTO? {
    return ticketStateLifecycle?.let {
        TicketManagerDTO(ticketId, ticketState, description, serialNumber,
        customerId, expertId, expertEmail, this.creationDate, this.lastModified, it, this.survey
        )
    }
}