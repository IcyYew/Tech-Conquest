package com.spring.app;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exception Controller class.
 * @author Michael Geltz
 */
@RestController
class Exception extends Throwable {

    @RequestMapping(method = RequestMethod.GET, path = "/oops")
    public String triggerException() {
        throw new RuntimeException("Check to see what happens when an exception is thrown");
    }

}
