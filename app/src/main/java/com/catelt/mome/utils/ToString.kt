package com.catelt.mome.utils

object ToString {
    fun timePosition(currentPosition: Long, duration: Long): String{
        val timeToBack = (duration - currentPosition)/1000
        val hour = timeToBack/3600
        val strHour: String = if (hour >= 10){
            "$hour"
        } else if(hour <= 0){
            ""
        }else{
            "0$hour"
        }
        val minus = timeToBack%3600/60
        val strMin = if (minus >= 10){
            "$minus"
        }else{
            "0$minus"
        }
        val sec = timeToBack%60
        val strSec = if (sec >= 10){
            "$sec"
        }else{
            "0$sec"
        }
        return if (strHour.isBlank()){
            "$strMin:$strSec"
        } else{
            "$strHour:$strMin:$strSec"
        }
    }
}