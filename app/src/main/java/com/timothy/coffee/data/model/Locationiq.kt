package com.timothy.coffee.data.model

data class Locationiq(
	val boundingbox: List<String?>? = null,
	val address: LocationiqAddress? = null,
	val lon: String? = null,
	val displayName: String? = null,
	val lat: String? = null
)
