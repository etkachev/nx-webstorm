package com.github.etkachev.nxwebstorm.models

class FormValueMap {
  var formVal: MutableMap<String, String> = mutableMapOf()

  fun valueGetter(key: String): () -> String {
    return { formVal[key] ?: "" }
  }

  fun valueSetter(key: String): (String) -> Unit {
    return { x: String -> setFormValueOfKey(key, x) }
  }

  fun nullValueGetter(key: String): () -> String? {
    return { formVal[key] }
  }

  fun nullValueSetter(key: String): (String?) -> Unit {
    return { x: String? -> setFormValueOfKey(key, x) }
  }

  fun boolValueGetter(key: String): () -> Boolean {
    return { formVal[key] == "true" }
  }

  fun boolValueSetter(key: String): (Boolean) -> Unit {
    return { x: Boolean -> formVal[key] = if (x) "true" else "false" }
  }

  fun setFormValueOfKey(key: String, value: String?) {
    formVal[key] = value ?: ""
  }
}
