package com.final_project.server.util

import com.final_project.server.exception.ErrorDetails
import com.final_project.server.exception.Exception
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class AttachmentAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception.FileNotExistException::class)
    fun productNotFoundError(e: Exception.FileNotExistException): ErrorDetails {
        return ErrorDetails(
            e.error()
        )
    }
}