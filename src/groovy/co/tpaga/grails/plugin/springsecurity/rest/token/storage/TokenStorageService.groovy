package co.tpaga.grails.plugin.springsecurity.rest.token.storage
/**
 * Implementations of this interface are responsible to load user information from a token storage system, and to store
 * token information into it.
 */
interface ApiKeyStorageService {

    /**
     * Returns a principal object given the passed token value
     * @throws ApiKeyNotFoundException if no api key is found in the storage
     */
    Object loadUserByApiKey(String tokenValue) throws ApiKeyNotFoundException

}
