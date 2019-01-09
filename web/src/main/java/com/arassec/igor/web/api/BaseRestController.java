package com.arassec.igor.web.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Base class for REST-Controllers.
 */
@Controller
@RequestMapping(value = "/api")
public abstract class BaseRestController {
}
