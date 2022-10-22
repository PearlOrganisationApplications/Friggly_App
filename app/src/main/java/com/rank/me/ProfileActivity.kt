@file:OptIn(ExperimentalMaterial3Api::class)

package com.rank.me

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.me.data.local.LocalData
import com.rank.me.ui.theme.Friggly_AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Friggly_AppTheme {
                // A surface container using the 'background' color from the theme
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top appbar
                    TopAppbarProfile(activity = (LocalContext.current as? Activity))
                    ProfileEcommerce()
                }
            }
        }
    }
}
private val optionsList: ArrayList<OptionsData> = ArrayList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppbarProfile(activity: Activity?) {
    TopAppBar(
        title = {
            Text(
                text = "Profile",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                activity?.finish()
            }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Go back",
                )
            }
        }
    )
}

@Composable
fun ProfileEcommerce(context: Context = LocalContext.current.applicationContext) {

    // This indicates if the optionsList has data or not
    // Initially, the list is empty. So, its value is false.
    var listPrepared by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            optionsList.clear()
            // Add the data to optionsList
            prepareOptionsData()

            listPrepared = true
        }
    }

    if (listPrepared) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {

            item {
                // User's image, name, email and edit button
                UserDetails(context = context)
            }

            // Show the options
            items(optionsList) { item ->
                OptionsItemStyle(item = item, context = context)
            }
        }
    }
}

// This composable displays user's image, name, email and edit button

@Composable
private fun UserDetails(context: Context) {
    val user = LocalData(context).getUser()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // User's image
        Image(
            modifier = Modifier
                .size(72.dp)
                .clip(shape = CircleShape),
            painter = painterResource(id = R.drawable.profile_placeholder),
            contentDescription = "Your Image"
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 3f, fill = false)
                    .padding(start = 16.dp)
            ) {
                // User's name
                Text(
                    text = (user.data?.firstName ?: "John") +" "+ (user.data?.lastName ?: "Doe"),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = FontFamily(Font(R.font.montserrat_semibold, FontWeight.SemiBold)),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // User's email
                Text(
                    text = user.data?.email ?:"email123@email.com",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.montserrat_semibold, FontWeight.Normal)),
                        color = Color.Gray,
                        letterSpacing = (0.8).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Edit button
            IconButton(
                modifier = Modifier
                    .weight(weight = 1f, fill = false),
                onClick = {
                    Toast.makeText(context, "Edit Button", Toast.LENGTH_SHORT).show()
                }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Row style for options
@Composable
private fun OptionsItemStyle(item: OptionsData, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true) {
                when (item.id) {
                    1 -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                    }
                   2 -> {
                        Toast.makeText(context, item.subTitle, Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(context, item.id.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icon
        Icon(
            modifier = Modifier
                .size(32.dp),
            imageVector = item.icon,
            contentDescription = item.title,
            tint = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 3f, fill = false)
                    .padding(start = 16.dp)
            ) {

                // Title
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_medium, FontWeight.Medium))
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Sub title
                Text(
                    text = item.subTitle,
                    style = TextStyle(
                        fontSize = 14.sp,
                        letterSpacing = (0.8).sp,
                        fontFamily = FontFamily(Font(R.font.montserrat_semibold, FontWeight.Normal)),
                        color = Color.Gray
                    )
                )

            }

            // Right arrow icon
            Icon(
                modifier = Modifier
                    .weight(weight = 1f, fill = false),
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = item.title,
                tint = Color.Black.copy(alpha = 0.70f)
            )
        }

    }
}

private fun prepareOptionsData() {

    val appIcons = Icons.Outlined

    optionsList.add(
        OptionsData(
            id = 1,
            icon = appIcons.Person,
            title = "Account",
            subTitle = "Manage your account"
        )
    )

    optionsList.add(
        OptionsData(
            id =2,
            icon = appIcons.Shield,
            title = "Manage Blocking",
            subTitle = "23 block contacts"
        )
    )

    optionsList.add(
        OptionsData(
            id =3,
            icon = appIcons.PanoramaFishEye,
            title = "Who viewed my profile",
            subTitle = "2 new notifications"
        )
    )

    optionsList.add(
        OptionsData(
            id =4,
            icon = appIcons.Payments,
            title = "Saved Cards",
            subTitle = "Your saved debit/credit cards"
        )
    )

    optionsList.add(
        OptionsData(
            id =5,
            icon = appIcons.Notifications,
            title = "Notifications",
            subTitle = "1 new notification"
        )
    )

    optionsList.add(
        OptionsData(
            id =6,
            icon = appIcons.Help,
            title = "Help Center",
            subTitle = "FAQs and customer support"
        )
    )

    optionsList.add(
        OptionsData(
            id =7,
            icon = appIcons.LocalOffer,
            title = "Friggly News",
            subTitle = "New app update release in..."
        )
    )

    optionsList.add(
        OptionsData(
            id =8,
            icon = appIcons.FavoriteBorder,
            title = "Wishlist",
            subTitle = "Items you saved"
        )
    )
}

data class OptionsData(val id: Int, val icon: ImageVector, val title: String, val subTitle: String)

