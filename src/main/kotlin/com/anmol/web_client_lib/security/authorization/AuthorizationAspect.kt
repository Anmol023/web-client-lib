package com.anmol.web_client_lib.security.authorization

import com.anmol.web_client_lib.expection_handling.ErrorResponse
import com.anmol.web_client_lib.expection_handling.ForbiddenException
import com.anmol.web_client_lib.security.CustomerAuthenticationDataUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


//Any method annotated with @Authorization will go through this aspect, and we will check if the user has the required role to perform the action, if not we will throw a ForbiddenException
@Aspect
@Component
class AuthorizationAspect(
    @Autowired val customerAuthenticationDataUtil: CustomerAuthenticationDataUtil
) {
    @Suppress("UNCHECKED_CAST")
    @Around("@annotation(authorization)")
    fun performAction(proceedingJoinPoint: ProceedingJoinPoint, authorization: Authorization): Mono<Any> {
        return customerAuthenticationDataUtil.fetchCustomerAuthenticationData()
            .flatMap { customerAuthenticationData ->
                val role = customerAuthenticationData.role
                val allowedRoles = authorization.allowedRoles
                val notAllowedRoles = authorization.notAllowedRoles
                if ((allowedRoles.isNotEmpty() && !allowedRoles.contains(role)) ||
                    (notAllowedRoles.isNotEmpty() && notAllowedRoles.contains(role))
                ) {
                    Mono.error(
                        ForbiddenException(
                            ErrorResponse(
                                message = "User not authorized for action",
                                errorCode = "AUTHORIZATION_ERROR"
                            ).toServiceError()
                        )
                    )
                } else proceedingJoinPoint.proceed() as Mono<Any>
            }
    }

}


