import javax.servlet.http.HttpServletResponse

security {

    rest {

        active = true

        login {
            active = true
            endpointUrl = '/api/login'
            usernamePropertyName = 'username'
            passwordPropertyName = 'password'
            failureStatusCode = HttpServletResponse.SC_UNAUTHORIZED    //401
            useJsonCredentials = true
            useRequestParamsCredentials = false
        }

        logout {
            endpointUrl = '/api/logout'
        }

        token {

            generation {
                useSecureRandom = true
                useUUID = false
            }

            storage {
                useJwt = true
                useGorm = false
                useMemcached = false
                useGrailsCache = false
                useRedis = false

                gorm {
                    tokenDomainClassName = null
                    tokenValuePropertyName = 'tokenValue'
                    usernamePropertyName = 'username'
                }

                memcached {
                    hosts = 'localhost:11211'
                    username = ''
                    password = ''

                    expiration = 3600
                }

                jwt {
                    useSignedJwt = true
                    useEncryptedJwt = false

                    secret = 'qrD6h8K6S9503Q06Y6Rfk21TErImPYqa'
                    expiration = 3600
                }

                redis {
                    expiration = 3600
                }

            }

            validation {
                active = true
                headerName = 'X-Auth-Token'
                endpointUrl = '/api/validate'
                tokenHeaderMissingStatusCode = HttpServletResponse.SC_UNAUTHORIZED    //401
                enableAnonymousAccess = false
                useBearerToken = true
            }

            rendering {
                usernamePropertyName = 'username'
                tokenPropertyName = 'access_token'
                authoritiesPropertyName = 'roles'
            }
        }
    }
}
