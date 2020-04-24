package com.timothy.coffee.data.model

data class Locationiq(
//	val osmType: String? = null,
//	val osmId: String? = null,
//	val licence: String? = null,
	val boundingbox: List<String?>? = null,
	val address: LocationiqAddress? = null,
	val lon: String? = null,
	val displayName: String? = null,
//	val placeId: String? = null,
	val lat: String? = null
)
