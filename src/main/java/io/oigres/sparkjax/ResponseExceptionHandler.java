/**********
 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the
 Free Software Foundation; either version 3.0 of the License, or (at your
 option) any later version. (See <https://www.gnu.org/licenses/gpl-3.0.html>.)

 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 more details.

 You should have received a copy of the GNU General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 **********/
// Copyright (c) 2020-2024 Sergio Exposito.  All rights reserved.
package io.oigres.sparkjax;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.http.HttpStatus;

import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

/**
 * @author Sergio Exposito
 */
public class ResponseExceptionHandler {
	private ResponseTransformer responseTransformer;
	private MediaType mediaType;
	private boolean includeStacktrace;
	private boolean includeException;

	public ResponseExceptionHandler(ResponseTransformer responseTransformer, MediaType mediaType, boolean includeStacktrace, boolean includeException) {
		super();
		this.responseTransformer = responseTransformer;
		this.mediaType = mediaType;
		this.includeStacktrace = includeStacktrace;
		this.includeException = includeException;
	}

	protected Map<String, Object> getErrorAttributes(Exception ex, HttpStatus.Code status, Request request) {
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("timestamp", new Date());
		addStatus(errorAttributes, status);
		addErrorDetails(errorAttributes, ex);
		addPath(errorAttributes, request);
		return errorAttributes;
	}
	
	private void addStatus(Map<String, Object> errorAttributes, HttpStatus.Code status) {
		if (status == null) {
			errorAttributes.put("status", 999);
			errorAttributes.put("error", "None");
			return;
		}
		errorAttributes.put("status", status);
		errorAttributes.put("error", HttpStatus.getMessage(status.getCode()));
	}
	
	private void addErrorDetails(Map<String, Object> errorAttributes, Throwable error) {
		if (error != null) {
			while (error instanceof ServletException && error.getCause() != null) {
				error = error.getCause();
			}
			if (this.includeException) {
				errorAttributes.put("exception", error.getClass().getName());
			}
			if (this.includeStacktrace) {
				addStackTrace(errorAttributes, error);
			}
		}
		addErrorMessage(errorAttributes, error);
	}

	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put("trace", stackTrace.toString());
	}

	private void addErrorMessage(Map<String, Object> errorAttributes, Throwable error) {
		List<Error> errors = extractErrors(error);
		if (errors == null || errors.isEmpty()) {
			addExceptionErrorMessage(errorAttributes, error);
		}
		else {
			addErrorsMessage(errorAttributes, errors);
		}
	}

	private List<Error> extractErrors(Throwable error) {
		if (error instanceof ConstraintViolationException) {
			ConstraintViolationException violation = (ConstraintViolationException)error;
			Set<ConstraintViolation<?>> constraints = violation.getConstraintViolations();
			List<Error> errors = new LinkedList<Error>();
			for (ConstraintViolation<?> constraint : constraints) {
				FieldError fieldError = new FieldError(constraint.getMessage(), 
														constraint.getLeafBean() != null ? constraint.getLeafBean().getClass().getSimpleName() : null, 
														constraint.getPropertyPath().toString(), 
														String.valueOf(constraint.getInvalidValue()));
				fieldError.setMessageTemplate(constraint.getMessageTemplate());
				if (constraint.getConstraintDescriptor() != null && constraint.getConstraintDescriptor().getAnnotation() != null) {
					fieldError.setSource(constraint.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
				}
				errors.add(fieldError);
			}
			return errors;
		}
		return Collections.emptyList();
	}
	
	private void addExceptionErrorMessage(Map<String, Object> errorAttributes, Throwable error) {
		Object message = null;
		if (error != null) {
			message = error.getMessage();
		}
		if (message == null && String.valueOf(message).trim().isEmpty()) {
			message = "No message available";
		}
		errorAttributes.put("message", message);
	}
	
	private void addErrorsMessage(Map<String, Object> errorAttributes, List<Error> errors) {
		errorAttributes.put("message", "Validation failed. " + "Error count: " + errors.size());
		errorAttributes.put("errors", errors);
	}
	
	private void addPath(Map<String, Object> errorAttributes, Request request) {
		String path = request.uri();
		if (path != null) {
			errorAttributes.put("path", path);
		}
	}

	protected void handleExceptionInternal(Exception ex, HttpStatus.Code status, Request request, Response response) {
		Map<String, Object> body = getErrorAttributes(ex, status, request);
		String bodyString = null;
		try {
			bodyString = this.responseTransformer.render(body);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		response.status(status.getCode());
		response.header(HttpHeaders.CONTENT_TYPE, this.mediaType.toString());
		response.body(bodyString);
	}
	
	public void handle(ConstraintViolationException exception, Request request, Response response) {
		handleExceptionInternal(exception, HttpStatus.Code.BAD_REQUEST, request, response);
	}

}
