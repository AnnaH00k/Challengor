package tech.hookin.learningkmp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.hookin.learningkmp.ui.components.CenteredRow
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage
import tech.hookin.learningkmp.ui.components.MainTextInput
import tech.hookin.learningkmp.ui.components.PasswordInput
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import tech.hookin.learningkmp.objects.User
import tech.hookin.learningkmp.ui.components.EmailInput


@Composable
fun Register(
    BackClick: () -> Unit,
    onRegisterSubmit: (name: String, email: String, password: String) -> Unit,
    registeredUsers: List<User>,
    onGoToLogin: () -> Unit
) {
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val isEmailValid = remember { mutableStateOf(false) }
    val isPasswordValid = remember { mutableStateOf(false) }
    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val usernameExists = remember { mutableStateOf(false) }
    val emailExists = remember { mutableStateOf(false) }

    fun checkUsername(name: String) {
        usernameExists.value = registeredUsers.any { it.name.equals(name, ignoreCase = true) }
    }
    fun checkEmail(email: String) {
        emailExists.value = registeredUsers.any { it.email.equals(email, ignoreCase = true) }
    }




    MainPage(background = "#779F7F", textColor = "#202226") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            MainButton(
                text = "Back",
                onClick = BackClick,
            )
        }
        H2("Register")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                MainTextInput(
                    value = nameState.value,
                    onValueChange = {
                        nameState.value = it
                        checkUsername(it)
                    },
                    label = "Name",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            emailFocusRequester.requestFocus()
                        }
                    )
                )
                if (usernameExists.value) {
                    Text(
                        text = "Username already taken",
                        color = Color.Red,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                EmailInput(
                    onValueChange = {
                        emailState.value = it
                        checkEmail(it)
                    },
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
                if (emailExists.value) {
                    Column (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "There is already an email with this address.",
                            color = Color.Red,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        MainButton(
                            text = "Go to Login",
                            onClick = onGoToLogin,
                            enabled = true
                        )

                    }
                }
            }





            PasswordInput(
                onValueChange = { passwordState.value = it },
                passwordValue = passwordState.value,
                onValidationChange = { isPasswordValid.value = it },
                focusRequester = passwordFocusRequester,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onRegisterSubmit(nameState.value, emailState.value, passwordState.value)
                    }
                )
            )
        }
        CenteredRow() {
            MainButton(
                text = "Register",
                enabled = isPasswordValid.value && !usernameExists.value && !emailExists.value,
                onClick = {
                    if (registeredUsers.any {
                            it.email.equals(
                                emailState.value,
                                ignoreCase = true
                            )
                        }) {
                        emailExists.value = true
                    } else {
                        onRegisterSubmit(nameState.value, emailState.value, passwordState.value)
                    }
                }
            )
        }
    }
}
