grails {
    plugin {
        springsecurity {

            filterChain {
                chainMap = [
                    '/api/**': 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter',
                    '/secured/**': 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter',
                    '/anonymous/**': 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor',
                    '/**': 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'
                ]
            }

            rest {
                token {
                    validation {
                        enableAnonymousAccess = true
                        useBearerToken = true
                    }

                    storage {
                        jwt {
                            useEncryptedJwt = true

                            privateKeyPath = "./grails-app/conf/private_key.der"
                            publicKeyPath = "./grails-app/conf/public_key.der"

                            expiration = 5
                        }
                    }
                }
            }
        }
    }
}

