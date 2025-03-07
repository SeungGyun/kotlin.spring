package com.nc.purple.works.mysql

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


import org.springframework.data.relational.core.mapping.Column

@Table("game")
data class Game(
    @Id
    val id: Long? = null,
    val gameCode: String,
    val groupKey: String,
    val isParent: Boolean,
    val parentGameCode: String? = null
)
