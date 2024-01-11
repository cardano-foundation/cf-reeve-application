package org.cardanofoundation.lob.app.support.web_support.internal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

// needed by Zalando Problem spring web
// https://www.baeldung.com/problem-spring-web
@ControllerAdvice
public class ExceptionResource implements ProblemHandling {
}
