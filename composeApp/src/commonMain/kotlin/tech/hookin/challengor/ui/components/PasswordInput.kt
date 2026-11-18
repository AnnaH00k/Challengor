package tech.hookin.challengor.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import tech.hookin.challengor.domain.validatePassword

/** Masked password field that shows a validation error once something has been typed. */
@Composable
fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    MainTextInput(
        value = value,
        onValueChange = onValueChange,
        label = "Password",
        modifier = modifier,
        errorMessage = value.takeIf { it.isNotEmpty() }?.let(::validatePassword),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = PasswordVisualTransformation(),
    )
}
