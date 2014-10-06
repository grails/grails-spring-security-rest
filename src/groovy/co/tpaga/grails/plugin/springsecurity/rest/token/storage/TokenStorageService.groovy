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
    Object loadUserByToken(String tokenValue) throws ApiKeyNotFoundException

    /**
     * Stores a token. It receives the principal to store any additional information together with the token,
     * like the username associated.
     *
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    void storeToken(String tokenValue, Object principal)

    /**
     * Removes a token from the storage.
     * @throws ApiKeyNotFoundException if the given api key is not found in the storage
     */
    void removeToken(String tokenValue) throws ApiKeyNotFoundException
}
