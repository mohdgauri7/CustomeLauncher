package com.mohdgauri.customelauncher.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mohdgauri.customelauncher.data.LayoutOption
import com.mohdgauri.customelauncher.data.model.AppInfo
import com.mohdgauri.customelauncher.screens.components.CircularProgressDemo
import com.mohdgauri.customelauncher.screens.components.SearchTextField
import com.mohdgauri.customelauncher.viewmodels.HomeViewModel


@Composable
fun HomeScreen() {

    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()

    LaunchedEffect(key1 = Unit) {
        viewModel.getInstalledApps(context)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }
    var selectedLayout by remember { mutableStateOf(LayoutOption.LINEAR_VERTICAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if(viewModel.homeData.isLoading){
            CircularProgressDemo()
            Text(text = "Loading...")
        }else{

            SearchTextField(
                query = searchQuery,
                onQueryChange = {query ->
                    searchQuery = query
                    viewModel.searchUpdate(searchQuery)
                }
            )

            if (viewModel.homeData.installedApps.isNotEmpty()){

                var mDisplayMenu by remember { mutableStateOf(false) }
                Box (modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd){

                    Row (modifier = Modifier.clickable {
                        mDisplayMenu = !mDisplayMenu
                    }){
                        Text(text = "Layout")

                        IconButton(
                            onClick = { mDisplayMenu = !mDisplayMenu },
                            modifier = Modifier.size(24.dp).rotate(90f)
                        ) {
                            Icon(
                                Icons.Default.MoreHoriz,
                                contentDescription = "More",
                                tint = Color.Black
                            )
                        }

                        DropdownMenu(
                            modifier = Modifier.background(Color.White),
                            expanded = mDisplayMenu,
                            onDismissRequest = { mDisplayMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Linear") },
                                onClick = {
                                    selectedLayout = LayoutOption.LINEAR_VERTICAL
                                    mDisplayMenu = false
                                },
                                leadingIcon = {
                                    if (selectedLayout == LayoutOption.LINEAR_VERTICAL){
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "selected",
                                            tint = Color.Green
                                        )
                                    }
                                }
                            )

                            Divider()

                            DropdownMenuItem(
                                text = { Text(text = "Grid") },
                                onClick = {
                                    selectedLayout = LayoutOption.GRID
                                    mDisplayMenu = false
                                },
                                leadingIcon = {
                                    if(selectedLayout == LayoutOption.GRID){
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Grid",
                                            tint = Color.Green
                                        )
                                    }
                                }
                            )
                        }

                    }

                }



                when (selectedLayout) {
                    LayoutOption.LINEAR_VERTICAL -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.homeData.installedApps.size) { index ->
                                val app = viewModel.homeData.installedApps[index]
                                AppItemLinear(app, viewModel)
                            }
                        }
                    }
                    LayoutOption.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.homeData.installedApps.size) { index ->
                                val app = viewModel.homeData.installedApps[index]
                                AppItemGrid(app = app, viewModel)
                            }
                        }
                    }
                }


            }else{
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Empty Apps List")
                }
            }

        }

    }
}

@Composable
fun AppItemGrid(app: AppInfo, viewModel: HomeViewModel) {
    val context = LocalContext.current
    var mDisplayMenu by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    mDisplayMenu = !mDisplayMenu
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Use Coil to load app icons
            // Display Bitmap icon
            Image(
                bitmap = app.icon.asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.name,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        DropdownMenu(
            modifier = Modifier.background(Color.White),
            expanded = mDisplayMenu,
            onDismissRequest = { mDisplayMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = "Launch")},
                onClick = {
                    viewModel.launchApp(context = context, packageName = app.packageName)
                    mDisplayMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Launch"
                    )
                }
            )

            Divider()

            DropdownMenuItem(
                text = { Text(text = "Uninstall") },
                onClick = {
                    viewModel.uninstallApp(context = context, packageName = app.packageName)
                    mDisplayMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            )
        }
    }

}


@Composable
fun AppItemLinear(app: AppInfo, viewModel: HomeViewModel) {
    val context = LocalContext.current
    var mDisplayMenu by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    mDisplayMenu = !mDisplayMenu
                }
        ) {
            Image(
                bitmap = app.icon.asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp).width(8.dp))
            Column {

                Text(
                    text = app.name,
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(1f))


            Row {

                IconButton(
                    onClick = { mDisplayMenu = !mDisplayMenu },
                    modifier = Modifier.size(24.dp).rotate(90f)
                ) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = Color.Black
                    )
                }


                DropdownMenu(
                    modifier = Modifier.background(Color.White),
                    expanded = mDisplayMenu,
                    onDismissRequest = { mDisplayMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Launch")},
                        onClick = {
                            viewModel.launchApp(context = context, packageName = app.packageName)
                            mDisplayMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Launch"
                            )
                        }
                    )

                    Divider()

                    DropdownMenuItem(
                        text = { Text(text = "Delete") },
                        onClick = {
                            viewModel.uninstallApp(context = context, packageName = app.packageName)
                            mDisplayMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }


            }


        }
    }

}

