package de.rosenau.simon.rockpaperscissors.util.exception

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestControllerAdvice
class HttpExceptionHandler {
    @ExceptionHandler(HttpException::class)
    fun handle(exception: HttpException, request: WebRequest): ResponseEntity<Any> {
        val contentType = getAcceptableMediaType(request)
        if (contentType == MediaType.APPLICATION_JSON) {
            val errorAttributes = LinkedHashMap<String, Any>();
            errorAttributes["timestamp"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
            errorAttributes["status"] = exception.status.value()
            errorAttributes["error"] = exception.message
            errorAttributes["path"] = request.getDescription(false).substring(4)
            return ResponseEntity.status(exception.status).contentType(contentType).body(errorAttributes)
        }

        // Fallback to plain text
        return ResponseEntity.status(exception.status).body(exception.message)
    }

    private fun getAcceptableMediaType(request: WebRequest): MediaType? {
        val accepts = request.getHeader("Accept") ?: return MediaType.APPLICATION_JSON
        if (accepts.contains(MediaType.APPLICATION_JSON_VALUE)) return MediaType.APPLICATION_JSON
        if (accepts.contains(MediaType.APPLICATION_NDJSON_VALUE)) return MediaType.APPLICATION_NDJSON
        return null
    }
}