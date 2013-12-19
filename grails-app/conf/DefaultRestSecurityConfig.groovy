import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

security {

    rest {

        active = true

        login {

            endpointUrl = '/login'
            usernameParameter = 'username'
            passwordParameter = 'password'

        }


        tokenRepository {

            //tokenDomainClassName = 'AuthenticationToken'
            //tokenValuePropertyName = 'tokenValue'

        }

    }

}