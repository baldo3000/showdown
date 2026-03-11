package me.baldo3000.showdown.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddressDTO(val ip: String, val port: Int)

@Serializable
data class LobbiesDTO(val lobbies: List<AddressDTO>)
