package co.tpaga.grails.plugin.springsecurity.rest.apiKey.storage
/**
 * Implementations of this interface are responsible to load user information from an Api Key storage system.
 */
interface ApiKeyStorageService {

    /**
     * Returns a principal object given the passed token value
     * @throws ApiKeyNotFoundException if no api key is found in the storage
     */
    Object loadUserByApiKey(String tokenValue) throws ApiKeyNotFoundException

}
