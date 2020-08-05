package com.github.etkachev.nxwebstorm.actions

import com.intellij.openapi.ui.Messages
import java.awt.event.ActionEvent

fun openSchematicButtonAction(id: String): (ActionEvent) -> Unit {
    return fun(_: ActionEvent): Unit {
        Messages.showMessageDialog("Opening schematic for $id", "Nx", Messages.getInformationIcon())
    }
}