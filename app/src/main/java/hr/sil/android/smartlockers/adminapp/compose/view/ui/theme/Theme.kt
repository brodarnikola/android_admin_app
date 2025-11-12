package hr.sil.android.smartlockers.adminapp.compose.view.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material3.MaterialTheme as Material3
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController




// *New design m3 - Themes*

// Light color scheme
val LightColorScheme = lightColorScheme(
    primary = Primary60,
    onPrimary = Primary100,
    primaryContainer = Primary90,
    onPrimaryContainer = Primary10,
    secondary = Secondary40,
    onSecondary = White,
    secondaryContainer = Secondary60,
    onSecondaryContainer = Secondary0,
    tertiary = Tertiary60,
    onTertiary = Tertiary0,
    tertiaryContainer = Tertiary90,
    onTertiaryContainer = Tertiary0,
    error = Error40,
    onError = Error100,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Neutral100,
    onBackground = Neutral0,
    surface = Neutral100,
    onSurface = Neutral10,
    surfaceVariant = Neutral99,
    onSurfaceVariant = Neutral50,
    outline = Neutral60,
    outlineVariant = Neutral90
)

// Dark color scheme
val DarkColorScheme = darkColorScheme(
    primary = Primary90,
    onPrimary = Primary20,
    primaryContainer = Primary40,
    onPrimaryContainer = Primary100,
    secondary = Secondary60,
    onSecondary = Secondary0,
    secondaryContainer = Secondary20,
    onSecondaryContainer = Secondary99,
    tertiary = Tertiary80,
    onTertiary = Tertiary0,
    tertiaryContainer = Tertiary50,
    onTertiaryContainer = Tertiary0,
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = Neutral0,
    onBackground = Neutral99,
    surface = Neutral0,
    onSurface = Neutral100,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral90,
    outline = Neutral60,
    outlineVariant = Neutral30
)

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)


private val LightColorPalette =
    AppColors(
        brandPrimary = Neutral0,
        brandSecondary = CustomPurple,
        brandThird = CustomGold,
        sendBubbleColor = CustomGray6,
        sendBubbleColoriMessage = CustomBlue,
        sendBubbleColorSms = SmsMmsLightBubbleColor,
        sendBubbleColorWhatsapp = WhatsAppLightBubbleColor,
        receiveBubbleColor = CustomGray1,
        uiBackground = Neutral0,
        uiBackground2 = CustomGray3,
        uiBackground3 = Neutral0,
        shadowedBackground = CustomGray1,
        shadowedBackground2 = CustomGray8,
        highlightedBackground = LightBlue,
        uiSwipeBackground1 = LightBlue,
        uiSwipeBackground2 = LightBlue,
        textPrimary = Neutral9,
        textSecondary = CustomGray2,
        textThird = CustomGray7,
        textInteractive = CustomPurple,
        textColorGoldBubble = DarkModeGray900,
        whatsApp = WhatsApp,
        smsMms = SmsMms,
        dialogText = DarkModeGray400,
        introductionBackgroundColor = Purple500,
        textFieldBackground = Neutral0,
        textFieldBorder = CustomGray2,
        uiBorder = Neutral4,
        backButton = CustomPurple,
        buttonDisconnect = CustomRed,
        buttonDisabled = CustomGray1,
        radioButtonSelected = CustomPurple,
        radioButtonUnselected = CustomPurple,
        dialogBackground = CustomGray5,
        dialogBackground2 = Neutral0,
        divider = CustomGray0,
        uiFloated = FunctionalDarkGrey,
        textHelp = CustomGray2,
        textLink = Ocean11,
        iconSecondary = Neutral7,
        iconInteractive = Neutral0,
        iconInteractiveInactive = Neutral1,
        errorDelete = FunctionalRed,
        gradient6_1 = listOf(Shadow4, Ocean3, Shadow2, Ocean3, Shadow4),
        gradient6_2 = listOf(Rose4, Lavender3, Rose2, Lavender3, Rose4),
        gradient3_1 = listOf(Shadow2, Ocean3, Shadow4),
        gradient3_2 = listOf(Rose2, Lavender3, Rose4),
        gradient2_1 = listOf(Shadow4, Shadow11),
        gradient2_2 = listOf(Ocean3, Shadow3),
        gradient2_3 = listOf(Lavender3, Rose2),
        tornado1 = listOf(Shadow4, Ocean3),
        statusBar = Neutral0,
        unpinBackground = CustomGray6,
        swipeDelete = FunctionalRed,
        dialogDeleteChat = FunctionalRed,
        dividerDialog = DarkModeGray100,
        isDark = false,
    )

private val DarkColorPalette =
    AppColors(
        brandPrimary = DarkModeGray900, //DarkModeBlue,
        brandSecondary = DarkModePurple,
        brandThird = CustomGold,
        sendBubbleColor = Neutral3,
        sendBubbleColoriMessage = DarkModeBlue,
        sendBubbleColorSms = SmsMmsBubbleColor,
        sendBubbleColorWhatsapp = WhatsAppBubbleColor,
        receiveBubbleColor = DarkModeGray750,
        uiBackground = DarkModeGray900,
        uiBackground2 = DarkModeGray750,
        uiBackground3 = DarkModeGray800,
        shadowedBackground = DarkModeGray700,
        shadowedBackground2 = DarkModeGray700,
        highlightedBackground = DarkModeGray600,
        uiSwipeBackground1 = LightBlue,
        uiSwipeBackground2 = LightBlue,
        textPrimary = DarkModeWhite,
        textSecondary = DarkModeGray400,
        textThird = DarkModeGray500,
        textInteractive = DarkModePurple800,
        textColorGoldBubble = DarkModeGray900,
        textFieldBackground = DarkModeTransparent,
        textFieldBorder = DarkModeGray400,
        dialogDeleteChat = DarkModePurple800,
        whatsApp = WhatsApp,
        smsMms = SmsMms,
        uiBorder = Neutral3,
        backButton = DarkModePurple800,
        buttonDisconnect = DarkModeRed,
        buttonDisabled = DarkModeGray600,
        radioButtonSelected = RadioButtonWhite,
        radioButtonUnselected = RadioButtonWhite,
        divider = DarkModeGray100,
        dividerDialog = CustomGray0,
        dialogBackground = DarkModeGray650,
        dialogBackground2 = DarkModeGray600,
        uiFloated = FunctionalDarkGrey,
        textHelp = Neutral1,
        textLink = Ocean2,
        iconPrimary = Shadow1,
        iconSecondary = Neutral0,
        iconInteractive = Neutral7,
        iconInteractiveInactive = Neutral6,
        errorDelete = FunctionalRedDark,
        gradient6_1 = listOf(Shadow5, Ocean7, Shadow9, Ocean7, Shadow5),
        gradient6_2 = listOf(Rose11, Lavender7, Rose8, Lavender7, Rose11),
        gradient3_1 = listOf(Shadow9, Ocean7, Shadow5),
        gradient3_2 = listOf(Rose8, Lavender7, Rose11),
        gradient2_1 = listOf(Ocean3, Shadow3),
        gradient2_2 = listOf(Ocean4, Shadow2),
        gradient2_3 = listOf(Lavender3, Rose3),
        tornado1 = listOf(Shadow4, Ocean3),
        statusBar = DarkModeGray900,
        unpinBackground = DarkModeGray200,
        swipeDelete = DarkModeSwipeDelete,
        dialogText = DarkModeGray500,
        introductionBackgroundColor = Purple700,
        isDark = true,
    )


object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Stable
class AppColors(
    brandPrimary: Color,
    brandSecondary: Color,
    brandThird: Color,
    sendBubbleColor: Color,
    sendBubbleColoriMessage: Color,
    sendBubbleColorSms: Color,
    sendBubbleColorWhatsapp: Color,
    receiveBubbleColor: Color,
    uiBackground: Color,
    uiBackground2: Color,
    uiBackground3: Color,
    shadowedBackground: Color,
    shadowedBackground2: Color,
    highlightedBackground: Color,
    uiSwipeBackground1: Color,
    uiSwipeBackground2: Color,
    textPrimary: Color,
    textSecondary: Color,
    textThird: Color,
    textInteractive: Color,
    textColorGoldBubble: Color,
    whatsApp: Color,
    smsMms: Color,
    dialogText: Color,
    introductionBackgroundColor: Color,
    textFieldBackground: Color,
    textFieldBorder: Color,
    dialogDeleteChat: Color,
    uiBorder: Color,
    backButton: Color,
    buttonDisconnect: Color,
    buttonDisabled: Color,
    radioButtonSelected: Color,
    radioButtonUnselected: Color,
    divider: Color,
    dividerDialog: Color,
    dialogBackground: Color,
    dialogBackground2: Color,
    unpinBackground: Color,
    swipeDelete: Color,
    gradient6_1: List<Color>,
    gradient6_2: List<Color>,
    gradient3_1: List<Color>,
    gradient3_2: List<Color>,
    gradient2_1: List<Color>,
    gradient2_2: List<Color>,
    gradient2_3: List<Color>,
    uiFloated: Color,
    interactivePrimary: List<Color> = gradient2_1,
    interactiveSecondary: List<Color> = gradient2_2,
    interactiveMask: List<Color> = gradient6_1,
    textHelp: Color,
    textLink: Color,
    tornado1: List<Color>,
    statusBar: Color,
    iconPrimary: Color = brandPrimary,
    iconSecondary: Color,
    iconInteractive: Color,
    iconInteractiveInactive: Color,
    errorDelete: Color,
    notificationBadge: Color = errorDelete,
    isDark: Boolean,
) {
    var gradient6_1 by mutableStateOf(gradient6_1)
        private set
    var gradient6_2 by mutableStateOf(gradient6_2)
        private set
    var gradient3_1 by mutableStateOf(gradient3_1)
        private set
    var gradient3_2 by mutableStateOf(gradient3_2)
        private set
    var gradient2_1 by mutableStateOf(gradient2_1)
        private set
    var gradient2_2 by mutableStateOf(gradient2_2)
        private set
    var gradient2_3 by mutableStateOf(gradient2_3)
        private set
    var brandPrimary by mutableStateOf(brandPrimary)
        private set
    var brandSecondary by mutableStateOf(brandSecondary)
        private set
    var brandThird by mutableStateOf(brandThird)
        private set
    var sendBubbleColor by mutableStateOf(sendBubbleColor)
        private set
    var sendBubbleColoriMessage by mutableStateOf(sendBubbleColoriMessage)
        private set
    var sendBubbleColorSms by mutableStateOf(sendBubbleColorSms)
        private set
    var sendBubbleColorWhatsapp by mutableStateOf(sendBubbleColorWhatsapp)
        private set
    var receiveBubbleColor by mutableStateOf(receiveBubbleColor)
        private set
    var uiBackground by mutableStateOf(uiBackground)
        private set
    var uiBackground2 by mutableStateOf(uiBackground2)
        private set
    var uiBackground3 by mutableStateOf(uiBackground3)
        private set
    var uiBorder by mutableStateOf(uiBorder)
        private set
    var backButton by mutableStateOf(backButton)
        private set
    var buttonDisabled by mutableStateOf(buttonDisabled)
        private set
    var buttonDisconnect by mutableStateOf(buttonDisconnect)
        private set
    var radioButtonSelected by mutableStateOf(radioButtonSelected)
        private set
    var radioButtonUnselected by mutableStateOf(radioButtonUnselected)
        private set
    var dialogBackground by mutableStateOf(dialogBackground)
        private set
    var dialogBackground2 by mutableStateOf(dialogBackground2)
        private set
    var uiSwipeBackground1 by mutableStateOf(uiSwipeBackground1)
        private set
    var uiSwipeBackground2 by mutableStateOf(uiSwipeBackground2)
        private set
    var uiFloated by mutableStateOf(uiFloated)
        private set
    var interactivePrimary by mutableStateOf(interactivePrimary)
        private set
    var interactiveSecondary by mutableStateOf(interactiveSecondary)
        private set
    var interactiveMask by mutableStateOf(interactiveMask)
        private set
    var textPrimary by mutableStateOf(textPrimary)
        private set
    var textSecondary by mutableStateOf(textSecondary)
        private set
    var textThird by mutableStateOf(textThird)
        private set
    var textColorGoldBubble by mutableStateOf(textColorGoldBubble)
        private set
    var whatsApp by mutableStateOf(whatsApp)
        private set
    var smsMms by mutableStateOf(smsMms)
        private set
    var textHelp by mutableStateOf(textHelp)
        private set
    var textInteractive by mutableStateOf(textInteractive)
        private set
    var tornado1 by mutableStateOf(tornado1)
        private set
    var textLink by mutableStateOf(textLink)
        private set
    var statusBar by mutableStateOf(statusBar)
        private set
    var iconPrimary by mutableStateOf(iconPrimary)
        private set
    var iconSecondary by mutableStateOf(iconSecondary)
        private set
    var iconInteractive by mutableStateOf(iconInteractive)
        private set
    var iconInteractiveInactive by mutableStateOf(iconInteractiveInactive)
        private set
    var errorDelete by mutableStateOf(errorDelete)
        private set
    var notificationBadge by mutableStateOf(notificationBadge)
        private set
    var isDark by mutableStateOf(isDark)
        private set
    var highlightedBackground by mutableStateOf(highlightedBackground)
        private set
    var shadowedBackground by mutableStateOf(shadowedBackground)
        private set
    var shadowedBackground2 by mutableStateOf(shadowedBackground2)
        private set
    var textFieldBackground by mutableStateOf(textFieldBackground)
        private set
    var textFieldBorder by mutableStateOf(textFieldBorder)
        private set
    var divider by mutableStateOf(divider)
        private set
    var unpinBackground by mutableStateOf(unpinBackground)
        private set
    var swipeDelete by mutableStateOf(swipeDelete)
        private set
    var dialogDeleteChat by mutableStateOf(dialogDeleteChat)
        private set
    var dialogText by mutableStateOf(dialogText)
        private set
    var introductionBackgroundColor by mutableStateOf(introductionBackgroundColor)
        private set
    var dividerDialog by mutableStateOf(dividerDialog)
        private set

    fun update(other: AppColors) {
        gradient6_1 = other.gradient6_1
        gradient6_2 = other.gradient6_2
        gradient3_1 = other.gradient3_1
        gradient3_2 = other.gradient3_2
        gradient2_1 = other.gradient2_1
        gradient2_2 = other.gradient2_2
        gradient2_3 = other.gradient2_3
        brandPrimary = other.brandPrimary
        brandSecondary = other.brandSecondary
        brandThird = other.brandThird
        sendBubbleColor= other.sendBubbleColor
        sendBubbleColoriMessage = other.sendBubbleColoriMessage
        sendBubbleColorSms = other.sendBubbleColorSms
        sendBubbleColorWhatsapp = other.sendBubbleColorWhatsapp
        receiveBubbleColor = other.receiveBubbleColor
        smsMms = other.smsMms
        whatsApp = other.whatsApp
        uiBackground = other.uiBackground
        uiBackground2 = other.uiBackground2
        uiBackground3 = other.uiBackground3
        buttonDisabled = other.buttonDisabled
        buttonDisconnect = other.buttonDisconnect
        radioButtonSelected = other.radioButtonSelected
        radioButtonUnselected = other.radioButtonUnselected
        dialogBackground = other.dialogBackground
        dialogBackground2 = other.dialogBackground2
        uiBorder = other.uiBorder
        uiSwipeBackground1 = other.uiSwipeBackground1
        uiSwipeBackground2 = other.uiSwipeBackground2
        uiFloated = other.uiFloated
        interactivePrimary = other.interactivePrimary
        interactiveSecondary = other.interactiveSecondary
        interactiveMask = other.interactiveMask
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        textThird = other.textThird
        textColorGoldBubble = other.textColorGoldBubble
        textHelp = other.textHelp
        textInteractive = other.textInteractive
        textLink = other.textLink
        tornado1 = other.tornado1
        iconPrimary = other.iconPrimary
        iconSecondary = other.iconSecondary
        iconInteractive = other.iconInteractive
        iconInteractiveInactive = other.iconInteractiveInactive
        errorDelete = other.errorDelete
        notificationBadge = other.notificationBadge
        isDark = other.isDark
        highlightedBackground = other.highlightedBackground
        shadowedBackground = other.shadowedBackground
        shadowedBackground2 = other.shadowedBackground2
        unpinBackground = other.unpinBackground
        textFieldBackground = other.textFieldBackground
        textFieldBorder = other.textFieldBorder
        swipeDelete = other.swipeDelete
        dialogDeleteChat = other.dialogDeleteChat
        dialogText = other.dialogText
        introductionBackgroundColor = other.introductionBackgroundColor
        dividerDialog = other.dividerDialog
    }

    fun copy(): AppColors =
        AppColors(
            gradient6_1 = gradient6_1,
            gradient6_2 = gradient6_2,
            gradient3_1 = gradient3_1,
            gradient3_2 = gradient3_2,
            gradient2_1 = gradient2_1,
            gradient2_2 = gradient2_2,
            gradient2_3 = gradient2_3,
            brandPrimary = brandPrimary,
            brandSecondary = brandSecondary,
            brandThird = brandThird,
            sendBubbleColor = sendBubbleColor,
            sendBubbleColoriMessage = sendBubbleColoriMessage,
            sendBubbleColorSms = sendBubbleColorSms,
            sendBubbleColorWhatsapp = sendBubbleColorWhatsapp,
            receiveBubbleColor = receiveBubbleColor,
            whatsApp = whatsApp,
            smsMms = smsMms,
            uiBackground = uiBackground,
            uiBackground2 = uiBackground2,
            uiBackground3 = uiBackground3,
            uiBorder = uiBorder,
            backButton = backButton,
            buttonDisconnect = buttonDisconnect,
            buttonDisabled = buttonDisabled,
            radioButtonSelected = radioButtonSelected,
            radioButtonUnselected = radioButtonUnselected,
            dialogBackground = dialogBackground,
            dialogBackground2 = dialogBackground2,
            uiSwipeBackground1 = uiSwipeBackground1,
            uiSwipeBackground2 = uiSwipeBackground2,
            uiFloated = uiFloated,
            interactivePrimary = interactivePrimary,
            interactiveSecondary = interactiveSecondary,
            interactiveMask = interactiveMask,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            textThird = textThird,
            textColorGoldBubble = textColorGoldBubble,
            textHelp = textHelp,
            textInteractive = textInteractive,
            textLink = textLink,
            tornado1 = tornado1,
            iconPrimary = iconPrimary,
            iconSecondary = iconSecondary,
            iconInteractive = iconInteractive,
            iconInteractiveInactive = iconInteractiveInactive,
            errorDelete = errorDelete,
            notificationBadge = notificationBadge,
            statusBar = Neutral7,
            isDark = isDark,
            highlightedBackground = highlightedBackground,
            shadowedBackground = shadowedBackground,
            shadowedBackground2 = shadowedBackground2,
            textFieldBackground = textFieldBackground,
            textFieldBorder = textFieldBorder,
            divider = divider,
            unpinBackground = unpinBackground,
            swipeDelete = swipeDelete,
            dialogDeleteChat = dialogDeleteChat,
            dialogText = dialogText,
            introductionBackgroundColor = introductionBackgroundColor,
            dividerDialog = dividerDialog
        )
}

@Composable
fun ProvideAppColors(colors: AppColors, content: @Composable () -> Unit) {
    val colorPalette = remember {
        // Explicitly creating a new object here so we don't mutate the initial [colors]
        // provided, and overwrite the values set in it.
        colors.copy()
    }
    colorPalette.update(colors)
    CompositionLocalProvider(LocalAppColors provides colorPalette, content = content)
}

private val LocalAppColors =
    staticCompositionLocalOf<AppColors> { error("No LocalColorsPalette provided") }


val IsAppInDarkTheme = compositionLocalOf<Boolean> { error("No IsAppInDarkTheme provided") }


@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colors =
        if (isDarkTheme) {
            LightColorPalette
        } else {
            LightColorPalette
        }

    val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        isDarkTheme -> LightColorScheme
        else -> LightColorScheme
    }

    val sysUiController = rememberSystemUiController()

    SideEffect {
        sysUiController.setSystemBarsColor(color = colorScheme.background)
    }

//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )

    ProvideAppColors(colors) {
        CompositionLocalProvider(IsAppInDarkTheme provides isDarkTheme) {
            Material3(
                colorScheme = colorScheme,
                typography = AppTypography,
                shapes = AppShapes,
                content = content
            )
        }
    }

}
