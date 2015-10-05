package grails.plugin.springsecurity.rest.error


interface CallbackErrorHandler {

    /**
     * Converts an error that occurs during the callback to a parameter map that will be returned to the frontend
     * @param e
     * @return
     */
    Map convert(Exception e)
}