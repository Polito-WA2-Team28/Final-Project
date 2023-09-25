package com.final_project.server.dto


import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

class CustomerFormRegistration(
    @field:NotBlank
    @field:NotNull
    @field:Size(max = 30)
    @field:Pattern(regexp = "^[A-Za-z]+\$")
    var name:String,

    @field:NotBlank
    @field:NotNull
    @field:Size(max = 30)
    @field:Pattern(regexp = "^[A-Za-z]+\$")
    val surname:String,

    @field:NotBlank
    @field:NotNull
    @field:Size(max = 20)
    @field:Pattern(regexp = "^[A-Za-z0-9]+\$")
    val username:String,

    @field:NotBlank
    @field:NotNull
    @field:Size(max = 30)
    val password:String,


    @field:DateTimeFormat
    @field:NotNull
    val birthDate: Date,

    @field:NotNull
    @field:Pattern(regexp = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
    val email:String,

    @field:NotBlank
    @field:NotNull
    @field:Pattern(regexp = "[-+0-9 ]{7,15}")
    val phoneNumber:String
) {

}


