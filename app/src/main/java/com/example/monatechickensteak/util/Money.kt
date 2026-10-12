package com.example.monatechickensteak.util

import java.util.Locale

fun Double.toRand(): String = "R" + String.format(Locale.US, "%.2f", this)