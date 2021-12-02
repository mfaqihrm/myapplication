package com.kinnoe.myapplication.networking

object ApiEndpoint {
    var BASEURL = "http://api.openweathermap.org/data/2.5/"
    var CurrentWeather = "weather?"
    var ListWeather = "forecast?"
    var Daily = "forecast/daily?"
    var UnitsAppid = "&units=metric&appid=339e7230c571af9b31bf3928993e4896"
    var UnitsAppidDaily = "&units=metric&cnt=15&appid=339e7230c571af9b31bf3928993e4896"
}
