package com.catelt.mome.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Credits(
    val cast: List<CastMember>?,
    val crew: List<CrewMember>?
)

fun Credits.getDirector(): CrewMember? {
    val directors = this.crew?.filter { member -> member.job == "Director" }
    return if (directors?.count() == 1) directors.first() else null
}

fun Credits.toStringCast(): String?{
    this.cast?.let { list ->
        var str = ""
        for (cast in list){
            if (cast == list.last()){
                str += cast.name
            }
            else{
                str += "${cast.name}, "
            }
        }
        return str
    }
    return null
}
