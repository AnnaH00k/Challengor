package tech.hookin.learningkmp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import tech.hookin.learningkmp.ui.components.CenteredRow
import tech.hookin.learningkmp.ui.components.EmailInput
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage
import tech.hookin.learningkmp.ui.components.MainTextInput
import tech.hookin.learningkmp.ui.components.PasswordInput

@Composable
fun Login(
    BackClick: () -> Unit,
    onLoginSubmit: (email: String, password: String) -> Boolean
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val showError = remember { mutableStateOf(false) }
    val isEmailValid = remember { mutableStateOf(false) }
    val isPasswordValid = remember { mutableStateOf(false) }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var lastLoginSuccess = remember { mutableStateOf(false) }

    fun tryLogin() {
        val success = onLoginSubmit(emailState.value, passwordState.value)
        lastLoginSuccess.value = success
        showError.value = !success
    }

    MainPage(background = "#BED8C3", textColor = "#202226") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            MainButton(
                text = "Back",
                onClick = BackClick,
            )
        }
        H2("Login")

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            EmailInput(
                onValueChange = { emailState.value = it },
                emailValue = emailState.value,
                onValidationChange = { isEmailValid.value = it },
                focusRequester = emailFocusRequester,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()
                    }
                ),
            )
            PasswordInput(
                onValueChange = { passwordState.value = it },
                passwordValue = passwordState.value,
                onValidationChange = { isPasswordValid.value = it },
                focusRequester = passwordFocusRequester,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            )
            if (showError.value) {
                Text(
                    text = "Invalid email or password",
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            CenteredRow {
                MainButton(
                    text = "Login",
                    enabled = isEmailValid.value && isPasswordValid.value,
                    onClick = {
                        tryLogin()  // Button -> Login
                    },
                )
            }
        }
    }
}
