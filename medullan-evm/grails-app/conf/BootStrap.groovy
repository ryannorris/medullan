import medullan.evm.Role
import medullan.evm.User
import medullan.evm.UserRole;

class BootStrap {
	
	def springSecurityService

    def init = { servletContext ->
		
		User defaultUser = new User(username: 'aetnaevm', password: "Wtlnc", enabled: true).save(failOnError: true);
		
		Role adminRole = new Role(authority: "ROLE_ADMIN").save(failOnError: true);
		
		UserRole.create(defaultUser, adminRole);
		
		
    }
    def destroy = {
    }
}
