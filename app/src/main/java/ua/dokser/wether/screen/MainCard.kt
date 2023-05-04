package ua.dokser.wether.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import ua.dokser.wether.R
import ua.dokser.wether.data.WetherModel
import ua.dokser.wether.ui.theme.MyBlue


@Composable
fun MainCard(currentDay: MutableState<WetherModel>,
             onClickSync: () -> Unit,
             onClickSearch:() -> Unit) {

    Column(
        modifier = Modifier
            .padding(5.dp)
    )
    {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            backgroundColor = MyBlue,
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        text = currentDay.value.time,
                        style = TextStyle(fontSize = 15.sp),
                        color = Color.White
                    )
                    AsyncImage(
                        model = "https:${currentDay.value.icon}",
                        contentDescription = "img2",
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 3.dp, end = 8.dp)
                    )
                }

                Text(
                    text = currentDay.value.city,
                    style = TextStyle(fontSize = 24.sp),
                    color = Color.White
                )
                Text(
                    if (currentDay.value.currentTemp.isNotEmpty())
                        "${currentDay.value.currentTemp}ºC"
                    else "${currentDay.value.maxTemp}ºC /${currentDay.value.minTemp}ºC",
                    style = TextStyle(fontSize = 55.sp),
                    color = Color.White
                )
                Text(
                    text = currentDay.value.condition,
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    IconButton(
                        onClick = {
                            onClickSearch.invoke()
                        })
                    {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "img3",
                            tint = Color.White
                        )

                    }

                    Text(
                        text = if (currentDay.value.currentTemp.isNotEmpty())
                            "${currentDay.value.maxTemp}ºC /${currentDay.value.minTemp}ºC"
                        else "︎︎",
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                          onClickSync.invoke()  //start fun from parameters
                        })
                    {
                        Icon(
                            painter = painterResource(R.drawable.baseline_cloud_sync_24),
                            contentDescription = "img3",
                            tint = Color.White
                        )

                    }

                }

            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(
    daysList: MutableState<List<WetherModel>>,
    currentDay: MutableState<WetherModel>
) {
    val tableList = listOf<String>("HOURS", "DAYS")
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {

        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { pos ->
                TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, pos))

            },
            backgroundColor = MyBlue,
            contentColor = Color.White
        ) {
            tableList.forEachIndexed { index, text ->
                Tab(selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(text = text)
                    }

                )
            }

        }
        HorizontalPager(
            count = tableList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when (index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay)
        }
    }
}

// make list by hours
private fun getWeatherByHours(hours: String): List<WetherModel> {
    if (hours.isEmpty()) return emptyList()
    val hoursArray = JSONArray(hours)
    val list = ArrayList<WetherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WetherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString(),
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "", "", ""
            )
        )
    }
    return list
}



