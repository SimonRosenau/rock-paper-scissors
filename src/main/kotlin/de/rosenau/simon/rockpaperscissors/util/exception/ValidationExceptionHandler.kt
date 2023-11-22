package de.rosenau.simon.rockpaperscissors.util.exception

import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestControllerAdvice
class ValidationExceptionHandler: ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorAttributes = LinkedHashMap<String, Any>();
        errorAttributes["timestamp"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
        errorAttributes["status"] = status.value()
        errorAttributes["error"] = exception.fieldErrors.map(DefaultMessageSourceResolvable::getDefaultMessage).toList()
        errorAttributes["path"] = request.getDescription(false).substring(4)
        return ResponseEntity.status(status).body(errorAttributes)
    }
}