package ua.dokser.wether

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import ua.dokser.wether.data.WetherModel
import ua.dokser.wether.screen.DialogSearch
import ua.dokser.wether.screen.MainCard
import ua.dokser.wether.screen.TabLayout
import ua.dokser.wether.ui.theme.WetherTheme

const val API_KEY = "fb4337a77eb44d499bf92910232704"
val defoultCity = "Khotyn"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WetherTheme {
                //state boolean for alert dialog
                val dialogState = remember {
                    mutableStateOf(false)
                }
                //make state for list
                val daysList = remember {
                    mutableStateOf(listOf<WetherModel>())
                }
                //make state for current day
                val currentDay = remember {
                    mutableStateOf(
                        WetherModel(
                            "", "", "", "", "",
                            "", "", "",
                        )
                    )
                }
                
                //start fun getting String from alertDialog
                
                if (dialogState.value){
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, this, daysList,currentDay)
                    })
                }
                getData(defoultCity, this, daysList,currentDay)

                Image(
                    painter = painterResource(id = R.drawable.sky1),
                    contentDescription = "sky",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.9f),
                    contentScale = ContentScale.FillBounds
                )
                Column {
                    MainCard(currentDay, onClickSync = {
                        getData(defoultCity, this@MainActivity, daysList,currentDay)
                    },
                    onClickSearch = {
                        dialogState.value = true
                    })
                    TabLayout(daysList, currentDay)
                }

            }
        }
    }
}

 private fun getData(
    city: String, context: Context,
    daysList: MutableState<List<WetherModel>>,
    currentDay: MutableState<WetherModel>
) {
    //1.make url from site
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "3" +
            "&aqi=no&alerts=no"
    //2. make queue
    val queue = Volley.newRequestQueue(context) // need context in getData!!!

    //3.make StringRequest
    val stringRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWetherByDays(response)
            // remember list in stateList and current day
            daysList.value = list
            currentDay.value = list[0]
        },
        { error ->
            Log.d("MyLog", "$error")
        }
    )
    //4.add request in queue
    queue.add(stringRequest)
}

// get list of day
 private fun getWetherByDays(response: String): List<WetherModel> {
    if (response.isEmpty()) return emptyList()
    //make list for return
    val list = ArrayList<WetherModel>()
    //make object from JSON
    val mainObject = JSONObject(response)
    //get city
    val city = mainObject.getJSONObject("location").getString("name")
    //get array days
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
    //fill arrayList
    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WetherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                item.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                // hours as String!!!
                item.getJSONArray("hour").toString()
            )
        )
    }
    //change current day
    list[0] = list[0].copy(
        currentTemp = mainObject.getJSONObject("current").getString("temp_c")
            .toFloat().toInt().toString(),
        time = mainObject.getJSONObject("current").getString("last_updated")
    )
    return list

}