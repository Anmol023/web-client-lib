package com.anmol.web_client_lib.expection_handling

import com.anmol.web_client_lib.logging.LogDetails
import com.anmol.web_client_lib.logging.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException

@ControllerAdvice
class ExceptionAdvisor {

    private val logger: Logger = Logger(this.javaClass)

    @ExceptionHandler(ServerWebInputException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun invalidWebInput(e: ServerWebInputException): ErrorResponse {
        logger.error(
            LogDetails(
            errorCode = "GENERIC_ERROR",
            message = "some error has occurred due to invalid or missing input: ${e.message}"
        ), e)
        return ErrorResponse("some error has occurred due to invalid or missing input", "GENERIC_ERROR")
    }

    @ExceptionHandler(BadDataException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun badData(e: BadDataException): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(DataNotFound::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun noData(e: DataNotFound): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(DataNotFoundESB::class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    fun noDataESB(e: DataNotFoundESB): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(UnprocessableEntityException::class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun unprocessableEntity(e: UnprocessableEntityException): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun illegalArgumentException(e: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(e.message.orEmpty(), "GENERIC_ERROR")
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun genericThrowable(e: Throwable): ErrorResponse {
        logger.error(LogDetails(
            errorCode = "GENERIC_ERROR",
            message = "some error has occurred: ${e.message.orEmpty()}"
        ), e)
        return ErrorResponse("some error has occurred", "GENERIC_ERROR")
    }

    @ExceptionHandler(BaseException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun baseException(e: BaseException): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun unauthorized(e: UnauthorizedException): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    fun forbiddenException(e: ForbiddenException): ErrorResponse {
        return ErrorResponse(e.message, e.errorCode, e.details)
    }

    @ExceptionHandler(ValidationException::class)
    @ResponseStatus(value = HttpStatus.EXPECTATION_FAILED)
    @ResponseBody
    fun validationException(e: ValidationException): ValidationErrorDetails {
        return e.validationErrors
    }

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onConstraintViolationException(e: WebExchangeBindException): ErrorResponse {
        return ErrorResponse(e.allErrors.map { it.defaultMessage }.joinToString(), "REQUEST_VALIDATION_ERROR")
    }
}