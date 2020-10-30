package com.github.etkachev.nxwebstorm.ui.formcontrols

import com.intellij.ui.TextFieldWithAutoCompletionListProvider

class BasicAutoCompleteProvider(list: Array<String>) : TextFieldWithAutoCompletionListProvider<String>(list.toList()) {
  override fun getLookupString(item: String): String {
    return item
  }
}
