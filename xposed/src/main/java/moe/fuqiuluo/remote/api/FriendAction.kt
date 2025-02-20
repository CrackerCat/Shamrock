package moe.fuqiuluo.remote.api

import com.tencent.mobileqq.profilecard.api.IProfileCardBlacklistApi
import com.tencent.mobileqq.qroute.QRoute
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.remote.action.handlers.GetFriendList
import moe.fuqiuluo.remote.action.handlers.GetStrangerInfo
import moe.fuqiuluo.remote.action.handlers.IsBlackListUin
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.respond
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Routing.friendAction() {
    getOrPost("/get_stranger_info") {
        val uin = fetchOrThrow("user_id")
        call.respondText(GetStrangerInfo(uin))
    }

    getOrPost("/get_friend_list") {
        val refresh = fetchOrNull("refresh")?.toBooleanStrictOrNull() ?: false
        call.respondText(GetFriendList(refresh))
    }

    get("/is_blacklist_uin") {
        val uin = fetchGetOrThrow("user_id")
        call.respondText(IsBlackListUin(uin))
    }
}