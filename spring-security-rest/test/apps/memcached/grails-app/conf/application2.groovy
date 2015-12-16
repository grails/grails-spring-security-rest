System.setProperty('useBearerToken', true.toString())

grails {
    plugin {
        springsecurity {

            filterChain {
                chainMap = [
                    [pattern: '/api/**',       filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter'],
                    [pattern: '/secured/**',   filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter'],
                    [pattern: '/anonymous/**', filters: 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor'],
                    [pattern: '/**',           filters: 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter']
                ]
            }

            rest {
                token {
                    storage {
                        useMemcached = true
                    }
                    validation {
                        enableAnonymousAccess = true
                        useBearerToken = true
                    }
                }
            }
        }
    }
}

