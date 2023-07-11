package com.final_project.ticketing.repository

import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.model.TicketStateEvolution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketStateEvolutionRepository : CrudRepository<TicketStateEvolution, Long>, JpaRepository<TicketStateEvolution, Long> {
    @Query("SELECT tse FROM TicketStateEvolution tse WHERE tse.ticket.id = :ticketId")
    fun findAllByTicketId(ticketId: Long): List<TicketStateEvolution>
}