package org.opencdmp.controllers.controllerhandler;

import org.opencdmp.commons.JsonHandlingService;
import gr.cite.tools.exception.*;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@ControllerAdvice
public class GlobalExceptionHandler {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(GlobalExceptionHandler.class));
	
	private final JsonHandlingService jsonHandlingService;

	public GlobalExceptionHandler(JsonHandlingService jsonHandlingService) {
		this.jsonHandlingService = jsonHandlingService;
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnexpectedErrors(Exception exception, WebRequest request) throws Exception {
		HandledException handled = this.handleException(exception, request);
		this.log(handled.getLevel(), exception, MessageFormat.format("returning code {0} and payload {1}", handled.getStatusCode(), handled.getMessage()));
		return new ResponseEntity<>(handled.getMessage(), handled.getStatusCode());
    }

	public void log(System.Logger.Level level, Exception e, String message) {
		if (level != null) {
			switch (level) {
				case TRACE:
					logger.trace(message, e);
					break;
				case DEBUG:
					logger.debug(message, e);
					break;
				case INFO:
					logger.info(message, e);
					break;
				case WARNING:
					logger.warn(message, e);
					break;
				case ERROR:
					logger.error(message, e);
					break;
				default:
					logger.error(e);
			}
		} else {
			logger.error(e);
		}
	}

	public HandledException handleException(Exception exception, WebRequest request) throws Exception {
		HttpStatus statusCode;
		Map<String, Object> result;
		System.Logger.Level logLevel;

		switch (exception){
			case MyNotFoundException myNotFoundException -> {
				logLevel = System.Logger.Level.DEBUG;
				statusCode = HttpStatus.NOT_FOUND;
				int code = myNotFoundException.getCode();
				if (code > 0) {
					result = Map.ofEntries(
							Map.entry("code", code),
							Map.entry("error", myNotFoundException.getMessage())
					);
				}
				else {
					result = Map.ofEntries(
							Map.entry("error", myNotFoundException.getMessage())
					);
				}
			}
			case MyUnauthorizedException myUnauthorizedException -> {
				logLevel = System.Logger.Level.DEBUG;
				statusCode = HttpStatus.UNAUTHORIZED;
				int code = myUnauthorizedException.getCode();
				if (code > 0) {
					result = Map.ofEntries(
							Map.entry("code", code),
							Map.entry("error", myUnauthorizedException.getMessage())
					);
				}
				else {
					result = Map.ofEntries(
							Map.entry("error", myUnauthorizedException.getMessage())
					);
				}
			}
			case MyForbiddenException myForbiddenException -> {
				logLevel = System.Logger.Level.DEBUG;
				statusCode = HttpStatus.FORBIDDEN;
				int code = myForbiddenException.getCode();
				if (code > 0) {
					result = Map.ofEntries(
							Map.entry("code", code),
							Map.entry("error", myForbiddenException.getMessage())
					);
				}
				else {
					result = Map.ofEntries(
							Map.entry("error", myForbiddenException.getMessage())
					);
				}
			}
			case MyValidationException myValidationException -> {
				logLevel = System.Logger.Level.DEBUG;
				statusCode = HttpStatus.BAD_REQUEST;
				int code = myValidationException.getCode();

				result = new HashMap<>();
				if (code > 0) result.put("code", code);
				if (myValidationException.getMessage() != null) result.put("error", myValidationException.getMessage());
				if (myValidationException.getErrors() != null) result.put("message", myValidationException.getErrors());
			}
			case MyApplicationException myApplicationException -> {
				logLevel = System.Logger.Level.ERROR;
				statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
				int code = myApplicationException.getCode();
				if (code > 0) {
					result = Map.ofEntries(
							Map.entry("code", code),
							Map.entry("error", myApplicationException.getMessage())
					);
				}
				else {
					result = Map.ofEntries(
							Map.entry("error", myApplicationException.getMessage())
					);
				}
			}
			default ->  {
				logLevel = System.Logger.Level.ERROR;
				statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
				result = Map.ofEntries(
						Map.entry("error", "System error")
				);
			}
		};
		String serialization = this.jsonHandlingService.toJsonSafe(result);
		return new HandledException(statusCode, serialization, logLevel);
	}
	
	public static class HandledException{
		public HttpStatus statusCode;
		public String message;
		public System.Logger.Level level;

		public HandledException(HttpStatus statusCode, String message, System.Logger.Level level) {
			this.statusCode = statusCode;
			this.message = message;
			this.level = level;
		}

		public HttpStatus getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(HttpStatus statusCode) {
			this.statusCode = statusCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public System.Logger.Level getLevel() {
			return level;
		}

		public void setLevel(System.Logger.Level level) {
			this.level = level;
		}
	}
}
