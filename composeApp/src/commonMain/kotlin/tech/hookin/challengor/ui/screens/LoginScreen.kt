package tech.hookin.challengor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import tech.hookin.challengor.domain.validateEmail
import tech.hookin.challengor.domain.validatePassword
import tech.hookin.challengor.ui.components.CenteredRow
import tech.hookin.challengor.ui.components.EmailInput
import tech.hookin.challengor.ui.components.ErrorText
import tech.hookin.challengor.ui.components.H2
import tech.hookin.challengor.ui.components.MainButton
import tech.hookin.challengor.ui.components.MainPage
import tech.hookin.challengor.ui.components.PasswordInput

/** [onLogin] returns `false` when the credentials are wrong. */
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (email: String, password: String) -> Boolean,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginFailed by remember { mutableStateOf(false) }
    val passwordFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val canSubmit = validateEmail(email) == null && validatePassword(password) == null
    fun submit() {
        if (!canSubmit) return
        focusManager.clearFocus()
        loginFailed = !onLogin(email, password)
    }

    MainPage {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            MainButton(text = "Back", onClick = onBack)
        }
        H2("Login")

        EmailInput(
            value = email,
            onValueChange = { email = it; loginFailed = false },
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
        )
        PasswordInput(
            value = password,
            onValueChange = { password = it; loginFailed = false },
            modifier = Modifier.focusRequester(passwordFocus),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        if (loginFailed) {
            ErrorText("Invalid email or password")
        }
        CenteredRow {
            MainButton(text = "Login", enabled = canSubmit, onClick = ::submit)
        }
    }
}
