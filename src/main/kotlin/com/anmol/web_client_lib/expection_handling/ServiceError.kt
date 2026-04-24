package com.anmol.web_client_lib.expection_handling

interface ServiceError {
    val errorCode: String
    val message: String
}

fun ServiceError.toBaseException() = BaseException(this)
