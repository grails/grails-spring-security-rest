package com

import org.apache.commons.lang.builder.HashCodeBuilder

class AppUserRole implements Serializable {

	private static final long serialVersionUID = 1

	AppUser appUser
	Role role

	boolean equals(other) {
		if (!(other instanceof AppUserRole)) {
			return false
		}

		other.appUser?.id == appUser?.id &&
		other.role?.id == role?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (appUser) builder.append(appUser.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static AppUserRole get(long appUserId, long roleId) {
		AppUserRole.where {
			appUser == AppUser.load(appUserId) &&
			role == Role.load(roleId)
		}.get()
	}

	static boolean exists(long appUserId, long roleId) {
		AppUserRole.where {
			appUser == AppUser.load(appUserId) &&
			role == Role.load(roleId)
		}.count() > 0
	}

	static AppUserRole create(AppUser appUser, Role role, boolean flush = false) {
		def instance = new AppUserRole(appUser: appUser, role: role)
		instance.save(flush: flush, insert: true)
		instance
	}

	static boolean remove(AppUser u, Role r, boolean flush = false) {
		if (u == null || r == null) return false

		int rowCount = AppUserRole.where {
			appUser == AppUser.load(u.id) &&
			role == Role.load(r.id)
		}.deleteAll()

		if (flush) { AppUserRole.withSession { it.flush() } }

		rowCount > 0
	}

	static void removeAll(AppUser u, boolean flush = false) {
		if (u == null) return

		AppUserRole.where {
			appUser == AppUser.load(u.id)
		}.deleteAll()

		if (flush) { AppUserRole.withSession { it.flush() } }
	}

	static void removeAll(Role r, boolean flush = false) {
		if (r == null) return

		AppUserRole.where {
			role == Role.load(r.id)
		}.deleteAll()

		if (flush) { AppUserRole.withSession { it.flush() } }
	}

	static constraints = {
		role validator: { Role r, AppUserRole ur ->
			if (ur.appUser == null) return
			boolean existing = false
			AppUserRole.withNewSession {
				existing = AppUserRole.exists(ur.appUser.id, r.id)
			}
			if (existing) {
				return 'userRole.exists'
			}
		}
	}

	static mapping = {
		id composite: ['role', 'appUser']
		version false
	}
}
