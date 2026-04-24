package com.anmol.web_client_lib.security.authorization

import com.anmol.web_client_lib.security.Role

/**
 * @param: allowedRoles: Roles for which function execution should be allowed
 * @param: notAllowedRoles: Roles for which function execution should not be allowed
 * Following Mis-Use Cases should be avoided.
    1. allowed []  notAllowed[]
    2. allowed [R1,R2]  notAllowed[R1,R3]
    3. allowed [R1,R2]  notAllowed[R3]
 * TODO
 * Need to add Annotation Processor to give the early feedback to developer if above mis cases are being used.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Authorization(
    val allowedRoles: Array<Role> = [],
    val notAllowedRoles: Array<Role> = []
)
