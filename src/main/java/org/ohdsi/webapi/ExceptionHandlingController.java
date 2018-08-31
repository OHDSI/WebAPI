package org.ohdsi.webapi;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.logging.event.UnassignRoleEvent;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.PersistenceException;
import java.net.ConnectException;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.SYSTEM_ERROR;

@ControllerAdvice
public class ExceptionHandlingController {

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @ExceptionHandler
    public ResponseEntity<JsonResult> exceptionHandler(HibernateException ex) {

        eventPublisher.publishEvent(new UnassignRoleEvent(this));
        JsonResult result = new JsonResult(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<JsonResult> exceptionHandler(PersistenceException ex) {

        eventPublisher.publishEvent(new UnassignRoleEvent(this));
        JsonResult result = new JsonResult(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<JsonResult> exceptionHandler(ConnectException ex) {

        eventPublisher.publishEvent(new UnassignRoleEvent(this));
        JsonResult result = new JsonResult(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception ex) {
        eventPublisher.publishEvent(new UnassignRoleEvent(this));
    }

}
