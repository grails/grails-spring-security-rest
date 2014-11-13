import com.AppUser
import com.Role
import com.AppUserRole

class BootStrap {

    def init = { servletContext ->
        def userRole = new Role(authority:'ROLE_USER').save(flush:true)
        def adminRole = new Role(authority:'ROLE_ADMIN').save(flush:true)

        def jimi = new AppUser(username:'jimi', password:'jimispassword')
        def alvaro = new AppUser(username:'115537660854424164575', password:'N/A')

        jimi.save(flush:true)
        alvaro.save(flush:true)

        AppUserRole.create jimi, userRole, true
        AppUserRole.create alvaro, userRole, true
        AppUserRole.create alvaro, adminRole, true

    }
    def destroy = {
    }
}
