package tech.hookin.learningkmp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PasswordInput(
    onValueChange: (String) -> Unit,
    passwordValue: String,
    onValidationChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val errorState = remember { mutableStateOf<String?>(null) }
    val passwordFocusRequester = remember { FocusRequester() }


    fun validatePassword(input: String): String? {
        if (input.any { it.isWhitespace() }) {
            return "Password cannot contain whitespace"
        }
        if (input.length < 6) {
            return "Password must be at least 6 characters"
        }
        val forbiddenChars = "<>{}()[];:'\"\\/"
        if (input.any { it in forbiddenChars }) {
            return "Password contains invalid characters"
        }
        return null
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        MainTextInput(
            value = passwordValue,
            onValueChange = { newValue ->
                onValueChange(newValue)
                val error = validatePassword(newValue)
                errorState.value = error
                onValidationChange(error == null)
            },
            label = "Password",
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        errorState.value?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
