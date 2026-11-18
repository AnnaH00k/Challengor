package tech.hookin.challengor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import tech.hookin.challengor.domain.validateEmail
import tech.hookin.challengor.domain.validatePassword
import tech.hookin.challengor.ui.components.CenteredRow
import tech.hookin.challengor.ui.components.EmailInput
import tech.hookin.challengor.ui.components.ErrorText
import tech.hookin.challengor.ui.components.H2
import tech.hookin.challengor.ui.components.MainButton
import tech.hookin.challengor.ui.components.MainPage
import tech.hookin.challengor.ui.components.MainTextInput
import tech.hookin.challengor.ui.components.PasswordInput

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    isNameTaken: (String) -> Boolean,
    isEmailTaken: (String) -> Boolean,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onGoToLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val nameTaken = name.isNotBlank() && isNameTaken(name)
    val emailTaken = email.isNotBlank() && isEmailTaken(email)
    val canSubmit = name.isNotBlank() && !nameTaken && !emailTaken &&
        validateEmail(email) == null && validatePassword(password) == null

    fun submit() {
        if (!canSubmit) return
        focusManager.clearFocus()
        onRegister(name, email, password)
    }

    MainPage {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            MainButton(text = "Back", onClick = onBack)
        }
        H2("Register")

        MainTextInput(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            errorMessage = if (nameTaken) "Username already taken" else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
        )
        EmailInput(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.focusRequester(emailFocus),
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
        )
        if (emailTaken) {
            ErrorText("There is already an account with this email address.")
            MainButton(text = "Go to Login", onClick = onGoToLogin)
        }
        PasswordInput(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.focusRequester(passwordFocus),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        CenteredRow {
            MainButton(text = "Register", enabled = canSubmit, onClick = ::submit)
        }
    }
}
