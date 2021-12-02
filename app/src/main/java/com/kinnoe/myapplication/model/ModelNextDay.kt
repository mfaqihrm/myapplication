package com.kinnoe.myapplication.model

import java.io.Serializable

class ModelNextDay : Serializable {
    var nameDay: String? = null
    var nameDate: String? = null
    var descWeather: String? = null
    var tempMax = 0.0
    var tempMin = 0.0
}