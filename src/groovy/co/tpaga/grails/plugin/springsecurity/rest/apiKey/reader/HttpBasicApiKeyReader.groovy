package co.tpaga.grails.plugin.springsecurity.rest.apiKey.reader

import org.springframework.security.authentication.BadCredentialsException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.crypto.codec.Base64

/**
 * Reads the Api Key from a Authorization HTTP Header
 */
class HTTPBasicApiKeyReader implements ApiKeyReader{

    /**
     * @return the ApiKey from the Authorization header, null otherwise
     */
    @Override
    String findApiKey(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            return null;
        }
        try {
            String[] tokens = extractAndDecodeHeader(header, request);
            assert tokens.length == 2;
            String apiKeyValue = tokens[0]
            return  apiKeyValue
        }catch (IOException ioe){
            log.debug "Bad Credentials: ${ioe.message}"
            return null
        }
    }

    /**
     * Decodes the header into an Api Key
     *
     * @throws org.springframework.security.authentication.BadCredentialsException if the Basic header is not present or is not valid Base64
     */
    private String[] extractAndDecodeHeader(String header, HttpServletRequest request) throws IOException {
        byte[] base64Token = header.substring(6).getBytes("UTF-8");
        byte[] decoded;
        try {
            decoded = Base64.decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }
        String token = new String(decoded, "UTF-8");
        int delim = token.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return [token.substring(0, delim), token.substring(delim + 1)] as String[];
    }

}
