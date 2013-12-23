security {

    rest {

        active = true

        login {

            endpointUrl = '/login'
            usernameParameter = 'username'
            passwordParameter = 'password'

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
                }

            }

        }

    }

}