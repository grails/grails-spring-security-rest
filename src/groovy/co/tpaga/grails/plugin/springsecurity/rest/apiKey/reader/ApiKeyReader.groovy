package co.tpaga.grails.plugin.springsecurity.rest.apiKey.reader

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

public interface ApiKeyReader {

    /**
     * Reads an Api Key (if any) from the request
     *
     * @param request the HTTP request
     * @param response the response, in case any status code has to be sent
     * @return the Api Key when found, null otherwise
     */
    String findApiKey(HttpServletRequest request, HttpServletResponse response)

}