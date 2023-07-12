package com.final_project.ticketing.model

import com.final_project.server.model.EntityBase
import com.final_project.ticketing.util.TicketState
import jakarta.persistence.*
import java.util.Date

@Entity
@Table
class TicketStateEvolution(
    @ManyToOne(fetch = FetchType.LAZY)
    var ticket: Ticket,
    @Enumerated(EnumType.STRING)
    var state: TicketState,
    @Temporal(value = TemporalType.TIMESTAMP)
    var timestamp:Date

):EntityBase<Long>(){}