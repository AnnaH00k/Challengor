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
fun EmailInput(
    onValueChange: (String) -> Unit,
    emailValue: String,
    onValidationChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val errorState = remember { mutableStateOf<String?>(null) }
    val emailFocusRequester = remember { FocusRequester() }
    val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    val forbiddenChars = "<>{}()[];:'\"\\/"

    fun validateEmail(input: String): String? {
        if (input.any{ it.isWhitespace() }) {
            return "Email cannot contain whitespace"
        }
        if (input.any { it in forbiddenChars }) {
            return "Email contains invalid characters"
        }
        if (!emailRegex.matches(input)) {
            return "Invalid email format"
        }
        return null
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        MainTextInput(
            value = emailValue,
            onValueChange = { newValue ->
                onValueChange(newValue)
                val error = validateEmail(newValue)
                errorState.value = error
                onValidationChange(error == null)
            },
            label = "Email",
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
