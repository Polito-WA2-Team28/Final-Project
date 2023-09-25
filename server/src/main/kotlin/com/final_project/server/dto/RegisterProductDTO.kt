package com.final_project.server.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
import java.util.UUID

class RegisterProductDTO(
    @field:NotNull
    @field:Digits(integer = 10, fraction = 0, message = "Product ID must be a numeric value greater than 0 with up to 10 integral digits")
    @field:Min(value = 1)
    val productId: Long,

    @field:NotNull
    val serialNumber: UUID
) {
}