package com.sx.cas.shiro.bean

import java.io.Serializable

/**
 * Created by Administrator on 2017/10/23 0023.
 */
data class User(
        var username: String,
        var password: String
): Serializable{
    var id: Long ?= null
}