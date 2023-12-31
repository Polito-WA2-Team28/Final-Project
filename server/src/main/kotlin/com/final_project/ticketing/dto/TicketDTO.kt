package com.final_project.ticketing.dto

import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.util.TicketState
import java.util.Date
import java.util.UUID

data class TicketDTO(
    val ticketId: Long?,
    var ticketState: TicketState,
    val description: String,
    val serialNumber: UUID,
    val customerId: UUID?,
    var expertId: UUID?,
    var expertEmail: String?,
    val creationDate: Date,
    val lastModified:Date,
    var survey:String?
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

    fun updateSurvey(survey: String) {
        this.survey = survey
    }
}


fun Ticket.toDTO() : TicketDTO {
    return TicketDTO(this.getId(), state, description, this.product.serialNumber,
                     this.customer.id, this.expert?.id, this.expert?.email, this.creationDate, this.lastModified, this.survey)
}