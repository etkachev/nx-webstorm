package com.github.etkachev.nxwebstorm.utils

/**
 * flatten array of maps into single map.
 */
fun <R, S> foldListOfMaps(maps: Array<Map<R, S>>): Map<R, S> {
  return maps.fold(mutableMapOf<R, S>(), { acc, e ->
    for (key in e.keys) {
      val info = e[key] ?: continue
      acc[key] = info
    }
    return@fold acc
  }).toMap()
}
