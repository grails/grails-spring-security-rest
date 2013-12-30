package org.grails.plugin.resource.util

import org.apache.commons.logging.LogFactory

/**
 * Manager of statistics on loading and mapping.
 *
 * @author Peter N. Steinmetz
 * Date: 3/31/13
 * Time: 12:07 PM
 */
class StatsManager {
  def log = LogFactory.getLog(StatsManager)

  Map statistics = [:]

  /**
   * Reset all statistics.
   */
  void resetStats() {
    statistics.clear()
  }

  /**
   * Get a statistic for category and subcategory.
   *
   * @param category
   * @param subcategory
   * @param defaultValue
   * @return
   */
  def getStatValue(category, subcategory, defaultValue = 0) {
    def cat = statistics[category]
    if (cat == null) {
      cat = [:]
      statistics[category] = cat
    }
    return cat[subcategory] != null ? cat[subcategory] : defaultValue
  }

  /**
   * Store a statistic value for category and subcategory.
   *
   * @param category
   * @param subcategory
   * @param value
   */
  void storeAggregateStat(category, subcategory, value) {
    def v = getStatValue(category, subcategory)
    statistics[category][subcategory] = v + value
  }

  /**
   * Dump statistics to log.
   */
  void dumpStats() {
    if (log.debugEnabled) {
      statistics.each { cat, subcats ->
        subcats.each { sc, v ->
          log.debug "  ${sc} = $v"
        }
      }
    }
  }

}
