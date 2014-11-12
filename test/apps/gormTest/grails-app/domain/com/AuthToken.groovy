package com

class AuthToken {
	String tokenValue
	String username
	//Date refreshed = new Date()  // intended to work with afterLoad example below

	static constraints = {
		tokenValue(nullable:false, maxSize:32)
		username(nullable:false)
	}

	// Grails' default optimistic locking mechanism is unwanted on this domain
	// the tokenValue should be unique and is always the way a token will be looked up so using it as the 'id/pkey' for the db row makes sense
	// Grails by default comes with ehcache configured so makes sense to use that for token lookups.
	static mapping = {
		version false
		id generator:'assigned', name: 'tokenValue'
		cache true
	}

	/*
		NOTE: this is an example approach for keeping a token fresh without to frequently doing DB updates
		It would most likely be paired with a Quartz job to remove stale tokens at some interval
	
	def afterLoad() {
		// if being accessed and it is more than a day since last marked as refreshed
		// and it hasn't been wiped out by Quartz job (it exists, duh)
		// then refresh it
		if (refreshed < new Date() -1) {
			refreshed = new Date()
			it.save()
		}
	}
	*/
}
