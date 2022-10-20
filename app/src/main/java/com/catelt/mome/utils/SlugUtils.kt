package com.catelt.mome.utils

import java.text.Normalizer

object SlugUtils {
    fun slugify(word: String, replacement: String = "-") = Normalizer
        .normalize(word, Normalizer.Form.NFD)
        .replace("[^\\p{ASCII}]".toRegex(), "")
        .replace("[^a-zA-Z0-9\\s]+".toRegex(), "").trim()
        .replace("\\s+".toRegex(), replacement)
        .toLowerCase()
}