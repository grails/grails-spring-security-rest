package org.grails.plugin.resource

/**
 * @author Marc Palmer (marc@grailsrocks.com)
 */
class URLUtils {
    
    /**
     * Take a base URI and a target URI and resolve target against the base
     * using the normal rules e.g. "../x", "./x" "x" results in a link relative to the base's folder
     * and / is app-absolute, and anything with a protocol :// is absolute
     *
     * Please note, I take full responsibility for the nastiness of this code. I could not 
     * find a nice way to do this, and I wanted to find an existing lib to do it. Its
     * certainly not my finest moment. Sorry. Rely on the MenuTagTests.
     *
     * It's quite ugly in there.
     */
    static String relativeURI(base, target) {
         new URI(base).resolve(new URI(target)).normalize().toString()
    }
    
    /**
     * Works out if url is relative, such that it would need to be corrected if
     * the file containing the url is moved
     */
    static Boolean isRelativeURL(url) {
        !url.startsWith('data:') &&
        !url.startsWith('#') && 
        !(url.indexOf('://') >= 0)
    }
}