package com.sx.cas.shiro.dao

import com.sx.cas.shiro.bean.User
import org.springframework.stereotype.Repository

/**
 * Created by Administrator on 2017/10/23 0023.
 */
@Repository
open class UserDao {
    fun getByName(name: String): User{
        return User("xiaobai","0")
    }
}