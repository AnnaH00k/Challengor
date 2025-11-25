package tech.hookin.learningkmp


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.hookin.learningkmp.ui.components.CenteredRow
import tech.hookin.learningkmp.ui.components.ChallengorHeadline
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.Header
import tech.hookin.learningkmp.ui.utils.Vh
import tech.hookin.learningkmp.ui.utils.getScreenHeightDp
import tech.hookin.learningkmp.ui.utils.getScreenWidthDp

@Composable
@Preview
fun Landingpage(
    modifier: Modifier = Modifier,
    NavigateToLogin: () -> Unit = {},
    NavigateToRegister: () -> Unit = {},
) {

    MainPage(background = "#BED8C3", textColor = "#202226") {
        val screenWidthDp = getScreenWidthDp()



        ChallengorHeadline()
        H2("Find and create your next challenge")



        CenteredRow {


            MainButton(
                text = "Login",
                onClick = NavigateToLogin,
            )
            MainButton(
                text = "Register",
                onClick = NavigateToRegister,
            )
        }


    }

}

