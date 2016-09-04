package grails.plugin.springsecurity.rest

import com.nimbusds.jose.crypto.MACSigner
import grails.plugin.springsecurity.rest.token.generation.jwt.AbstractJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.generation.jwt.DefaultRSAKeyProvider
import grails.plugin.springsecurity.rest.token.generation.jwt.EncryptedJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.generation.jwt.NoOpCustomClaimProvider
import grails.plugin.springsecurity.rest.token.generation.jwt.RSAKeyProvider
import grails.plugin.springsecurity.rest.token.generation.jwt.SignedJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.storage.jwt.JwtTokenStorageService
import grails.spring.BeanBuilder

trait TokenGeneratorSupport {

    SignedJwtTokenGenerator setupSignedJwtTokenGenerator() {
        String secret = 'foobar'*10
        def jwtTokenStorageService = new JwtTokenStorageService(jwtService: new JwtService(jwtSecret: secret))
        return new SignedJwtTokenGenerator(defaultExpiration: 3600, jwtSecret: secret, signer: new MACSigner(secret), jwtTokenStorageService: jwtTokenStorageService, customClaimProvider: new NoOpCustomClaimProvider())
    }

    EncryptedJwtTokenGenerator setupEncryptedJwtTokenGenerator() {
        RSAKeyProvider keyProvider = new DefaultRSAKeyProvider()
        def jwtTokenStorageService = new JwtTokenStorageService(jwtService: new JwtService(keyProvider: keyProvider))
        return new EncryptedJwtTokenGenerator(defaultExpiration: 3600, jwtTokenStorageService: jwtTokenStorageService, keyProvider: keyProvider, customClaimProvider: new NoOpCustomClaimProvider())
    }

    AbstractJwtTokenGenerator getTokenGenerator(boolean useEncryptedJwt) {
        BeanBuilder beanBuilder = new BeanBuilder()
        beanBuilder.beans {
            keyProvider(DefaultRSAKeyProvider)
            customClaimProvider(NoOpCustomClaimProvider)

            jwtService(JwtService) {
                keyProvider = ref('keyProvider')
                jwtSecret = 'foo123'*8
            }
            tokenStorageService(JwtTokenStorageService) {
                jwtService = ref('jwtService')
            }

            if (useEncryptedJwt) {
                tokenGenerator(EncryptedJwtTokenGenerator) {
                    jwtTokenStorageService = ref('tokenStorageService')
                    keyProvider = ref('keyProvider')
                    defaultExpiration = 3600
                    customClaimProvider = ref('customClaimProvider')
                }
            } else {
                tokenGenerator(SignedJwtTokenGenerator) {
                    jwtTokenStorageService = ref('tokenStorageService')
                    jwtSecret = 'foo123'*8
                    defaultExpiration = 3600
                    customClaimProvider = ref('customClaimProvider')
                }
            }
        }

        return beanBuilder.createApplicationContext().getBean(AbstractJwtTokenGenerator, 'tokenGenerator')
    }


}