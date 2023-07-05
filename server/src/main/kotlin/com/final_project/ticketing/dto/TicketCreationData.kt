package com.final_project.ticketing.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
import java.util.UUID

class TicketCreationData(
    @field:NotBlank
    @field:NotNull
    @field:Size(max = 500)
    var description:String,

    @field:NotNull
    var serialNumber:UUID
){

}