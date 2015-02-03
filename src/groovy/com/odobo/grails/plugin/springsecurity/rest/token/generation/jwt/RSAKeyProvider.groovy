package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

/**
 * Implementations of this interface must take care of providing a pair of RSA private/public keys
 */
interface RSAKeyProvider {

    RSAPublicKey getPublicKey()

    RSAPrivateKey getPrivateKey()

}