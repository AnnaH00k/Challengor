package tech.hookin.challengor.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import tech.hookin.challengor.domain.validateEmail

/** Email field that shows a validation error once something has been typed. */
@Composable
fun EmailInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    MainTextInput(
        value = value,
        onValueChange = onValueChange,
        label = "Email",
        modifier = modifier,
        errorMessage = value.takeIf { it.isNotEmpty() }?.let(::validateEmail),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = imeAction),
        keyboardActions = keyboardActions,
    )
}
