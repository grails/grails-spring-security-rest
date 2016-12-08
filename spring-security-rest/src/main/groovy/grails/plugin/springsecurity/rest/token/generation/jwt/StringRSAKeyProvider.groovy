package grails.plugin.springsecurity.rest.token.generation.jwt;

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.InitializingBean

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

/**
 * Loads RSA public/private key's from configuration strings
 */
@Slf4j
@CompileStatic
class StringRSAKeyProvider implements RSAKeyProvider, InitializingBean {

    /** Full path to the public key so that {@code new File(publicKeyPath).exists() == true} */
    String publicKeyStr

    /** Full path to the private key so that {@code new File(publicKeyPath).exists() == true} */
    String privateKeyStr

    RSAPublicKey publicKey
    RSAPrivateKey privateKey

    @Override
    void afterPropertiesSet() throws Exception {
        log.debug "Loading public/private key from DER files"
        KeyFactory kf = KeyFactory.getInstance("RSA")

        log.debug "Public key: ${publicKeyStr}"
        if (publicKeyStr) {
            def keyBytes = publicKeyStr.bytes
            def spec = new X509EncodedKeySpec(keyBytes)
            publicKey = kf.generatePublic(spec) as RSAPublicKey
        }

        log.debug "Private key: ${privateKeyStr}"
        if (privateKeyStr) {
            def keyBytes = privateKeyStr.bytes
            def spec = new PKCS8EncodedKeySpec(keyBytes)
            privateKey = kf.generatePrivate(spec) as RSAPrivateKey
        }
    }

}
