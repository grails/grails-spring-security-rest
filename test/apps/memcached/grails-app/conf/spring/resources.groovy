import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder
import org.springframework.security.core.userdetails.memory.InMemoryDaoImpl
import org.springframework.security.provisioning.InMemoryUserDetailsManager

// Place your Spring DSL code here
beans = {

    userDetailsService(InMemoryDaoImpl) {
        userMap = 'jimi=jimispassword,ROLE_USER,ROLE_ADMIN,enabled'
    }

    passwordEncoder(PlaintextPasswordEncoder)

}
