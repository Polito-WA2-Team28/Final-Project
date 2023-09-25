package com.final_project.server.dto

import com.final_project.server.model.Expert
import com.final_project.ticketing.util.ExpertiseFieldEnum
import java.util.*

data class ExpertDTO (
    val id: UUID,
    val email: String,
    val username: String,
    val expertiseFields: MutableSet<ExpertiseFieldEnum>
) {}

fun Expert.toDTO(): ExpertDTO {
    return ExpertDTO(id, email, username, expertiseFields)
}