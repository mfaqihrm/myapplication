package com.kinnoe.myapplication.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.kinnoe.myapplication.R
import com.kinnoe.myapplication.adapter.MainAdapter
import com.kinnoe.myapplication.fragment.FragmentNextDays
import com.kinnoe.myapplication.model.ModelMain
import com.kinnoe.myapplication.networking.ApiEndpoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), LocationListener {

    private var lat: Double? = null
    private var lng: Double? = null
    private var hariIni: String? = null
    private var mProgressBar:ProgressDialog? = null
    private var mainAdapter: MainAdapter? = null
    private var modelMain:MutableList<ModelMain> = ArrayList()
    var permissionArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set transparent statusbar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        //set permission
        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                        requestPermissions(permissionArray, 101)
              }
        }

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEE", dateNow) as String

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Mohon tungggu")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Sedang menampilkan data...")

        val fragmentNextDays = FragmentNextDays.newInstance("FragmentNextDays")
        mainAdapter = MainAdapter(modelMain)

        rvListWeather.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        rvListWeather.setHasFixedSize(true)
        rvListWeather.setAdapter(mainAdapter)

        fabNextDays.setOnClickListener {
            fragmentNextDays.show(supportFragmentManager, fragmentNextDays.tag)
        }

        getToday()
        getLatlong()

    }

    private fun getToday() {
        val date = Calendar.getInstance().time
        val tanggal = android.text.format.DateFormat.format("d MMM yyyy", date) as String
        val formatDate = "$hariIni, $tanggal"
        tvDate.text = formatDate
    }

    private fun getLatlong() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 115)
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = provider?.let { locationManager.getLastKnownLocation(it) }
        if (location != null) {
            onLocationChanged(location)
        } else {
            provider?.let { locationManager.requestLocationUpdates(it, 20000, 0f, this) }
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude

        //method get Data Weather
        getCurrentWeather()
        getListWeather()
    }

    private fun getCurrentWeather() {
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.CurrentWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonArrayOne = response.getJSONArray("weather") //menandai array weather []
                        val jsonObjectOne = jsonArrayOne.getJSONObject(0)  // dari arrray didefinisikan ke object
                        val jsonObjectTwo = response.getJSONObject("main") //main : (temp, feels like, temp max, temp min dll
                        val jsonObjectThree = response.getJSONObject("wind") //wind : (speed, deg, gus
                        val strWeather = jsonObjectOne.getString("main")      //weather : main = Rain
                        val strDescWeather = jsonObjectOne.getString("description")  //weather : description = Very Heavy Rain
                        val strKecepatanAngin = jsonObjectThree.getString("speed") //wind : speed =
                        val strKelembaban = jsonObjectTwo.getString("humidity") //main : humidity =
                        val strNamaKota = response.getString("name")  //nama kota tipe String
                        val dblTemperatur = jsonObjectTwo.getDouble("temp") //main : temp =

                        if (strDescWeather == "broken clouds") {
                            iconTemp.setAnimation(R.raw.broken_clouds)
                            tvWeather.text = "Awan Tersebar"
                        } else if (strDescWeather == "light rain") {
                            iconTemp.setAnimation(R.raw.light_rain)
                            tvWeather.text = "Gerimis"
                        } else if (strDescWeather == "haze") {
                            iconTemp.setAnimation(R.raw.broken_clouds)
                            tvWeather.text = "Berkabut"
                        } else if (strDescWeather == "overcast clouds") {
                            iconTemp.setAnimation(R.raw.overcast_clouds)
                            tvWeather.text = "Awan Mendung"
                        } else if (strDescWeather == "moderate rain") {
                            iconTemp.setAnimation(R.raw.moderate_rain)
                            tvWeather.text = "Hujan Ringan"
                        } else if (strDescWeather == "few clouds") {
                            iconTemp.setAnimation(R.raw.few_clouds)
                            tvWeather.text = "Berawan"
                        } else if (strDescWeather == "heavy intensity rain") {
                            iconTemp.setAnimation(R.raw.heavy_intentsity)
                            tvWeather.text = "Hujan Lebat"
                        } else if (strDescWeather == "clear sky") {
                            iconTemp.setAnimation(R.raw.clear_sky)
                            tvWeather.text = "Cerah"
                        } else if (strDescWeather == "scattered clouds") {
                            iconTemp.setAnimation(R.raw.scattered_clouds)
                            tvWeather.text = "Awan Tersebar"
                        } else {
                            iconTemp.setAnimation(R.raw.unknown)
                            tvWeather.text = strWeather
                        }

                        tvNamaKota.text = strNamaKota
                        tvTempeatur.text = String.format(Locale.getDefault(), "%.0f°C", dblTemperatur)
                        tvKecepatanAngin.text = "Kecepatan Angin $strKecepatanAngin km/j"
                        tvKelembaban.text = "Kelembaban $strKelembaban %"
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Gagal menampilkan data header!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getListWeather() {
        mProgressBar?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.ListWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        mProgressBar?.dismiss()
                        val jsonArray = response.getJSONArray("list")
                        for (i in 0..6) {
                            val dataApi = ModelMain()
                            val objectList = jsonArray.getJSONObject(i)
                            val jsonObjectOne = objectList.getJSONObject("main")
                            val jsonArrayOne = objectList.getJSONArray("weather")
                            val jsonObjectTwo = jsonArrayOne.getJSONObject(0)
                            var timeNow = objectList.getString("dt_txt")
                            val formatDefault = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val formatTimeCustom = SimpleDateFormat("kk:mm")

                            try {
                                val timesFormat = formatDefault.parse(timeNow)
                                timeNow = formatTimeCustom.format(timesFormat)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                            dataApi.timeNow = timeNow
                            dataApi.currentTemp = jsonObjectOne.getDouble("temp")
                            dataApi.descWeather = jsonObjectTwo.getString("description")
                            dataApi.tempMin = jsonObjectOne.getDouble("temp_min")
                            dataApi.tempMax = jsonObjectOne.getDouble("temp_max")
                            modelMain.add(dataApi)
                        }
                        mainAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkIfAlreadyhavePermission() : Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLatlong()
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}