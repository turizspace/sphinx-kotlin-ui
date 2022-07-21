package chat.sphinx.common.components

import CommonButton
import Roboto
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import chat.sphinx.common.state.ContactScreenState
import chat.sphinx.common.viewmodel.contact.AddContactViewModel
import chat.sphinx.common.viewmodel.DashboardViewModel
import chat.sphinx.common.viewmodel.contact.EditContactViewModel
import chat.sphinx.common.viewmodel.contact.QRCodeViewModel
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.utils.SphinxFonts
import chat.sphinx.utils.getPreferredWindowSize
import chat.sphinx.wrapper.dashboard.ContactId
import com.example.compose.badge_red
import com.example.compose.light_divider

@Composable
fun AddContactWindow(dashboardViewModel: DashboardViewModel) {
    var isOpen by remember { mutableStateOf(true) }
    var screenState: ContactScreenState? = dashboardViewModel.contactWindowStateFlow.value.second

    if (isOpen) {
        Window(
            onCloseRequest = {
                dashboardViewModel.toggleContactWindow(false, null)
            },
            title = "",
            state = WindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                size = getPreferredWindowSize(420, 580)

            )
        ) {
            when (screenState) {
                is ContactScreenState.Choose -> AddContact(dashboardViewModel)
                is ContactScreenState.NewToSphinx -> AddNewContactOnSphinx()
                is ContactScreenState.AlreadyOnSphinx -> ContactForm(dashboardViewModel, false, null)
                is ContactScreenState.EditContact -> ContactForm(dashboardViewModel, true, screenState.contactId)
            }
        }
    }
}

@Composable
fun AddContact(dashboardViewModel: DashboardViewModel) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(75.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommonButton(
                callback = {
                    dashboardViewModel.toggleContactWindow(true, ContactScreenState.NewToSphinx)
                },
                text = "New to Sphinx",
                backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                enabled = true
            )
            Divider(Modifier.padding(12.dp), color = Color.Transparent)
            CommonButton(
                callback = {
                    dashboardViewModel.toggleContactWindow(true, ContactScreenState.AlreadyOnSphinx)
                },
                text = "Already on Sphinx",
                enabled = true
            )
        }
    }
}

@Composable
fun AddNewContactOnSphinx() {

    var nicknameText by remember { mutableStateOf("") }
    var includeMessageText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "NICKNAME",
                fontSize = 10.sp,
                fontFamily = SphinxFonts.montserratFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nicknameText,
                onValueChange = { nicknameText = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = Roboto
                ),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = light_divider,
                    unfocusedBorderColor = light_divider
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "INCLUDE A MESSAGE",
                fontSize = 10.sp,
                fontFamily = SphinxFonts.montserratFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = includeMessageText,
                onValueChange = { includeMessageText = it },
                modifier = Modifier.fillMaxWidth().height(108.dp),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 16.sp
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = light_divider,
                    unfocusedBorderColor = light_divider
                ),
                placeholder = {
                    Text(
                        text = "Welcome to Sphinx!",
                        color = Color.Gray,
                    )
                }
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "ESTIMATED COST",
                        fontSize = 12.sp,
                        fontFamily = Roboto,
                        color = Color.Gray,
                    )
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "2 000",
                            fontSize = 20.sp,
                            fontFamily = Roboto,
                            color = Color.White,
                        )
                        Text(
                            text = "sat",
                            fontSize = 20.sp,
                            fontFamily = Roboto,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                    },
                    modifier = Modifier.clip(CircleShape)
                        .wrapContentWidth()
                        .height(50.dp),

                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                {
                    Text(
                        text = "Create Invitation",
                        color = Color.White,
                        fontFamily = Roboto
                    )
                }
            }
        }
    }
}

@Composable
fun ContactForm(dashboardViewModel: DashboardViewModel, editMode: Boolean, contactId: ContactId?) {

    val viewModel = if(editMode){
        remember { EditContactViewModel(contactId) }
    } else {
        remember { AddContactViewModel() }
    }


//    var switchState = remember {
//        mutableStateOf(false)
//    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (editMode) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(112.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PhotoUrlImage(
                        photoUrl = viewModel.contactState.photoUrl,
                        modifier = Modifier.size(96.dp).clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            Column {
                Text(
                    text = "Nickname*",
                    fontSize = 12.sp,
                    fontFamily = Roboto,
                    color = Color.Gray,
                )
                BasicTextField(
                    value = viewModel.contactState.contactAlias,
                    onValueChange = {
                        viewModel.onNicknameTextChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.White, fontFamily = Roboto),
                    singleLine = true,
                    cursorBrush = SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.secondary)

                )
                Divider(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column {
                Text(
                    text = "Address*",
                    fontSize = 12.sp,
                    fontFamily = Roboto,
                    color = Color.Gray,
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = viewModel.contactState.lightningNodePubKey,
                        onValueChange = {
                            viewModel.onAddressTextChanged(it)
                        },
                        enabled = !editMode,
                        modifier = Modifier.weight(1f).padding(top = 12.dp),
                        textStyle = TextStyle(fontSize = 18.sp, color = Color.White, fontFamily = Roboto),
                        singleLine = true,
                        cursorBrush = SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                    )
                    if(editMode) {
                        IconButton(onClick = {
                            dashboardViewModel.toggleQRWindow(true)
                        }
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                }
                Divider(modifier = Modifier.padding(top = 4.dp), color = Color.Gray)

            }

            Spacer(modifier = Modifier.height(28.dp))

            Column {
                Text(
                    text = "Route Hint",
                    fontSize = 12.sp,
                    fontFamily = Roboto,
                    color = Color.Gray,
                )
                BasicTextField(
                    value = viewModel.contactState.lightningRouteHint ?: "",
                    onValueChange = {
                        viewModel.onRouteHintTextChanged(it)
                    },
                    enabled = !editMode,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.White, fontFamily = Roboto),
                    singleLine = true,
                    cursorBrush = SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.secondary)

                )
                Divider(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

//            Column {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = "Privacy Settings",
//                        fontSize = 12.sp,
//                        fontFamily = Roboto,
//                        color = Color.Gray,
//                    )
//                    Icon(
//                        Icons.Default.HelpOutline,
//                        contentDescription = "Privacy Settings",
//                        tint = Color.Gray,
//                        modifier = Modifier.size(20.dp).padding(start = 4.dp)
//                    )
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = "Standard PIN / Privacy PIN",
//                        fontSize = 18.sp,
//                        fontFamily = Roboto,
//                        color = Color.LightGray,
//                    )
//                    Switch(
//                        checked = switchState.value,
//                        onCheckedChange = { switchState.value = it },
//                        enabled = false
//                    )
//                }
//            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.fillMaxWidth().height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.contactState.status is Response.Error) {
                        Text(
                            text = "There was an error, please try again later",
                            fontSize = 12.sp,
                            fontFamily = Roboto,
                            color = badge_red,
                        )
                    }
                    if (viewModel.contactState.status is LoadResponse.Loading) {
                        CircularProgressIndicator(
                            Modifier.padding(start = 8.dp).size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CommonButton(
                    enabled = viewModel.contactState.saveButtonEnabled,
                    text = if (editMode) {
                        "SAVE"}
                    else {"SAVE TO CONTACTS"},
                    callback = {
                        viewModel.saveContact()
                    }
                )
            }
        }
    }

    if (viewModel.contactState.status is Response.Success) {
        dashboardViewModel.toggleContactWindow(false, null)
    }
    if (dashboardViewModel.qrWindowStateFlow.collectAsState().value){
        QRDetail(dashboardViewModel, QRCodeViewModel(viewModel.contactState.lightningNodePubKey, null))
    }
}

@Composable
fun QRDetail(
    dashboardViewModel: DashboardViewModel,
    viewModel: QRCodeViewModel
){
    val density = LocalDensity.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current


    var isOpen by remember { mutableStateOf(true) }
    if (isOpen) {
        Window(
            onCloseRequest = {
                dashboardViewModel.toggleQRWindow(false)
            },
            title = "",
            state = WindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                size = getPreferredWindowSize(357, 550)
            ),
            resizable = false
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(viewModel.contactQRCodeState.pubKey))
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 38.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PUBLIC KEY",
                        fontFamily = SphinxFonts.montserratFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(end = 24.dp)
                    ) {
                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = "QR Code",
                            tint = Color.Gray,
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            text = "CLICK TO COPY",
                            fontFamily = SphinxFonts.montserratFamily,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    viewModel.contactQRCodeState.bitMatrix?.let { bitMatrix ->
                        val width = bitMatrix.width
                        val height = bitMatrix.height

                        val widthDp = with(density) { width.toDp() }
                        val heightDp = with(density) { height.toDp() }

                        Box(modifier = Modifier.height(heightDp).width(widthDp)) {
                            Canvas(modifier = Modifier.height(heightDp).width(widthDp)) {
                                for (x in 0 until width) {
                                    for (y in 0 until height) {
                                        drawRect(
                                            brush = SolidColor(if (bitMatrix.get(x, y)) Color.Black else Color.White),
                                            topLeft = Offset(x.toFloat(), y.toFloat()),
                                            size = Size(1f, 1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        text = viewModel.contactQRCodeState.pubKey,
                        maxLines = 2,
                        fontFamily = SphinxFonts.montserratFamily,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

