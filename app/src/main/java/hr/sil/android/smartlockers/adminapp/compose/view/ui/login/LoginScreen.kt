package hr.sil.android.smartlockers.adminapp.compose.view.ui.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme as Material3
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.compose.view.ui.components.NewDesignButton
import hr.sil.android.smartlockers.adminapp.compose.view.ui.theme.AppTypography
import hr.sil.android.smartlockers.adminapp.compose.view.ui.theme.DarkModeTransparent
import hr.sil.android.smartlockers.adminapp.compose.view.ui.theme.White
import hr.sil.android.smartlockers.adminapp.compose.view.ui.utils.UiEvent
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    nextScreen: (route: String) -> Unit = {},
    navigateUp: (route: String) -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val activity = LocalContext.current as Activity

    // Properties
    val imageCheck = painterResource(id = R.drawable.ic_email)
    val imageInfo = painterResource(id = R.drawable.ic_email)
    val imageVisibilityOn = painterResource(id = R.drawable.ic_password)
    val imageVisibilityOff = painterResource(id = R.drawable.ic_password)

    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var isButtonEnabled by remember {
        mutableStateOf(true)
    }
    var errorMessageEmail by remember {
        mutableStateOf<String?>(null)
    }
    var errorMessagePassword by remember {
        mutableStateOf<String?>(null)
    }

    val emailLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val passwordLabelStyle = remember {
        mutableStateOf(AppTypography.bodyLarge)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        val log = logger()
        log.info("collecting events: start ${viewModel.uiEvents}")
        log.info("collecting events: viewModel ${viewModel}")
        viewModel.uiEvents.collect { event ->
            log.info("collecting event: ${event}")
            when (event) {

                is LoginScreenUiEvent.NavigateToMainActivityScreen -> {
                    val startIntent = Intent(context, MainActivity::class.java)
                    context.startActivity(startIntent)
                    activity.finish()
                }

                is UiEvent.ShowToast -> {
                    isButtonEnabled = true
                    Toast.makeText(context, event.message, event.toastLength).show()
                }
            }
        }
    }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 30.dp)
    ) {
        val (mainContent, bottomButton) = createRefs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(mainContent) {
                    top.linkTo(parent.top)
                    bottom.linkTo(bottomButton.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                    height = Dimension.fillToConstraints
                }
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Email Label
                Text(
                    text = stringResource(R.string.app_generic_username).uppercase(),
                    style = AppTypography.bodyMedium,
                    color = colorResource(R.color.colorBlack),
                    modifier = Modifier.fillMaxWidth()
                )

                // Email TextField with Icon
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        val checkErrorMessageEmail = viewModel.getEmailError(it, context)
                        errorMessageEmail =
                            if (checkErrorMessageEmail !== "") checkErrorMessageEmail else null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "emailTextFieldLoginScreen"
                        },
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Material3.colorScheme.onSurface,
                        backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = colorResource(R.color.colorPrimary),
                        focusedIndicatorColor = colorResource(R.color.colorPrimary),
                        unfocusedIndicatorColor = colorResource(R.color.colorDarkGray)
                    ),
                    textStyle = AppTypography.bodyLarge,
                    trailingIcon = {
                        Icon(
                            painter = imageInfo,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(R.color.colorDarkGray)
                        )
                    }
                )

                // Email Error Message
                if (errorMessageEmail != null && errorMessageEmail!!.isNotEmpty()) {
                    Text(
                        text = errorMessageEmail ?: "",
                        style = AppTypography.bodySmall,
                        color = Material3.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Password Label
                Text(
                    text = stringResource(R.string.app_generic_password).uppercase(),
                    style = AppTypography.bodyMedium,
                    color = colorResource(R.color.colorBlack),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password TextField with Icon
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        val checkErrorMessage = viewModel.getPasswordError(it, context)
                        errorMessagePassword = if (checkErrorMessage !== "") {
                            checkErrorMessage
                        } else {
                            null
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "passwordTextFieldLoginScreen"
                        },
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Material3.colorScheme.onSurface,
                        backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = colorResource(R.color.colorPrimary),
                        focusedIndicatorColor = colorResource(R.color.colorPrimary),
                        unfocusedIndicatorColor = colorResource(R.color.colorDarkGray)
                    ),
                    textStyle = AppTypography.bodyLarge,
                    trailingIcon = {
                        Icon(
                            painter = imageVisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(R.color.colorDarkGray)
                        )
                    }
                )

                // Password Error Message
                if (errorMessagePassword != null && errorMessagePassword!!.isNotEmpty()) {
                    Text(
                        text = errorMessagePassword ?: "",
                        style = AppTypography.bodySmall,
                        color = Material3.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Show Passwords button (centered, underlined) - Press and hold to reveal
                Text(
                    text = stringResource(R.string.intro_register_show_password),
                    style = AppTypography.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    color = colorResource(R.color.colorBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    passwordVisible = true
                                    tryAwaitRelease()
                                    passwordVisible = false
                                }
                            )
                        }
                        .padding(vertical = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Forgot Password button (centered, underlined)
//                Text(
//                    text = stringResource(R.string.forgot_password_title),
//                    style = AppTypography.bodyMedium.copy(
//                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
//                    ),
//                    color = colorResource(R.color.colorBlack),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable(
//                            interactionSource = remember { MutableInteractionSource() },
//                            indication = null
//                        ) {
//                            viewModel.onEvent(LoginScreenEvent.OnForgotPassword)
//                        }
//                        .padding(vertical = 4.dp)
//                        .semantics {
//                            contentDescription = "forgotPasswordButtonLoginScreen"
//                        },
//                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
//                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                if (state.loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = colorResource(R.color.colorPrimary),
                            strokeWidth = 3.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.heightIn(min = 30.dp))

            }
        }

        //region SignInButton
        NewDesignButton(
            modifier = Modifier
                .width(210.dp)
                .constrainAs(bottomButton) {
                    bottom.linkTo(parent.bottom, margin = 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .semantics {
                    contentDescription = "signInButtonLoginScreen"
                },
            title = stringResource(R.string.app_generic_sign_in),
            onClick = {
                val emailValidation = viewModel.getEmailError(email, context)
                val passwordValidation = viewModel.getPasswordError(password, context)

                if (emailValidation.isNotBlank() || passwordValidation.isNotBlank()) {
                    errorMessageEmail = emailValidation.ifBlank { null }
                    errorMessagePassword = passwordValidation.ifBlank { null }
                } else {
                    isButtonEnabled = false
                    viewModel.onEvent(
                        LoginScreenEvent.OnLogin(
                            email = email,
                            password = password,
                            context = context,
                            activity = activity
                        )
                    )
                }
            },
            enabled = isButtonEnabled,
        )

    }
}
