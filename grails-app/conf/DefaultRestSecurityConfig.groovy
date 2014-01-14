import javax.servlet.http.HttpServletResponse

security {

    rest {

        active = true

        login {

            endpointUrl = '/login'
            usernameParameter = 'username'
            passwordParameter = 'password'
            failureStatusCode = HttpServletResponse.SC_FORBIDDEN
            useRequestParamsCredentials = true
            useJsonCredentials = false

        }

        token {

            generation {
                useSecureRandom = true
                useUUID = false
            }

            storage {

                useGorm = false
                useMemcached = false

                gorm {
                    tokenDomainClassName = 'AuthenticationToken'
                    tokenValuePropertyName = 'tokenValue'
                    usernamePropertyName = 'username'
                }

                memcached {
                    hosts = 'localhost:11211'
                    username = ''
                    password = ''

                    expiration = 3600
                }

            }

            validation {
                headerName = 'X-Auth-Token'
                endpointUrl = '/validate'
            }

        }

    }

}