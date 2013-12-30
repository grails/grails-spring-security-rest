package org.grails.plugin.resource.util
/**
 * Utility class with static methods for manipulating dispositions on Requests.
 *
 * @author Peter N. Steinmetz
 * Date: 3/18/13
 * Time: 4:41 PM
 */
class DispositionsUtils {

  static REQ_ATTR_DISPOSITIONS_REMAINING = 'resources.dispositions.remaining'
  static REQ_ATTR_DISPOSITIONS_DONE = "resources.dispositions.done"

  static DISPOSITION_HEAD = 'head'
  static DISPOSITION_DEFER = 'defer'
  static DEFAULT_DISPOSITION_LIST = [DISPOSITION_HEAD, DISPOSITION_DEFER]

  /**
   * Get the set of dispositions required by resources in the current request, which have not
   * yet been rendered.
   *
   * @param request to retrieve dispositions from
   */
  static Set getRequestDispositionsRemaining(request) {
    def dispositions = request[REQ_ATTR_DISPOSITIONS_REMAINING]
    // Return a new set of HEAD + DEFER if there is nothing in the request currently, this is our baseline
    if (dispositions == null) {
      dispositions = new HashSet()
      request[REQ_ATTR_DISPOSITIONS_REMAINING] = dispositions
    }
    return dispositions
  }

  /**
   * Add a disposition to the request's set of them.
   *
   * @param to add disposition to
   * @param disposition to add
   * @param reason disposition is being added
   * @throws IllegalArgumentException if disposition has already been rendered
   */
  static void addDispositionToRequest(request, String disposition, String reason) {
    if (haveAlreadyDoneDispositionResources(request, disposition)) {
      throw new IllegalArgumentException("""Cannot disposition [$disposition] to this request (required for [$reason]) -
that disposition has already been rendered. Check that your r:layoutResources tag comes after all
Resource tags that add content to that disposition.""")
    }
    def dispositions = request[REQ_ATTR_DISPOSITIONS_REMAINING]
    if (dispositions != null) {
      dispositions << disposition
    } else {
      request[REQ_ATTR_DISPOSITIONS_REMAINING] = [disposition] as Set
    }
  }

  /**
   * Remove a disposition from the current request's set.
   *
   * @param request to remove disposition from
   * @param dispostion to remove
   */
  static void removeDispositionFromRequest(request, String disposition) {
    def dispositions = request[REQ_ATTR_DISPOSITIONS_REMAINING]
    if (dispositions != null) {
      dispositions.remove(disposition)
    }
  }

  /**
   * Move a disposition from the list to be performed for a request
   * to the list of those which are done.
   *
   * @param request with disposition to move
   * @param disposition to move
   */
  static void doneDispositionResources(request, String disposition) {
    removeDispositionFromRequest(request, disposition)
    def s = request[REQ_ATTR_DISPOSITIONS_DONE]
    if (s == null) {
      s = new HashSet()
      request[REQ_ATTR_DISPOSITIONS_DONE] = s
    }
    s << disposition
  }

  /**
   * Check if disposition's resources have already been rendered for a request.
   *
   * @param request
   * @param disposition
   * @return true if already resources have been rendered, false otherwise.
   */
  static boolean haveAlreadyDoneDispositionResources(request,String disposition) {
    def s = request[REQ_ATTR_DISPOSITIONS_DONE]
    s == null ? false : s.contains(disposition)
  }

}
