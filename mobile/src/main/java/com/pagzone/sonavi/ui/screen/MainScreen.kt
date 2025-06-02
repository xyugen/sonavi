package com.pagzone.sonavi.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pagzone.sonavi.R
import com.pagzone.sonavi.ui.screen.page.AddSoundPage
import com.pagzone.sonavi.ui.screen.page.DashboardPage
import com.pagzone.sonavi.ui.screen.page.LibraryPage
import com.pagzone.sonavi.ui.theme.Typography

@Preview(showSystemUi = true)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {

    val navItemList = listOf(
        NavItem(
            "Dashboard",
            ImageVector.vectorResource(id = R.drawable.ic_dashboard)
        ),
        NavItem(
            "Add Sound",
            ImageVector.vectorResource(id = R.drawable.ic_circle_plus)
        ),
        NavItem(
            "Library",
            ImageVector.vectorResource(id = R.drawable.ic_music)
        ),
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                navItem.icon,
                                contentDescription = navItem.label,
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = navItem.label,
                                style = Typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        ),
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        }
                    )
                }
            }
        }) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> DashboardPage(modifier)
        1 -> AddSoundPage(modifier)
        2 -> LibraryPage(modifier)
    }
}