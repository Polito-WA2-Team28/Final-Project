package com.final_project.server.util

import com.final_project.server.exception.ErrorDetails
import com.final_project.server.exception.Exception
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MaxUploadSizeExceededException

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

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleFileSizeException(e: MaxUploadSizeExceededException): ErrorDetails {
        return ErrorDetails("This attachment's size exceeds the maximum size for a file upload")
    }

}