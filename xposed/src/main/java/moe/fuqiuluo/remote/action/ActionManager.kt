package moe.fuqiuluo.remote.action

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.fuqiuluo.remote.action.handlers.*
import moe.fuqiuluo.remote.entries.EmptyObject
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString
import moe.fuqiuluo.xposed.tools.*

internal object ActionManager {
    val actionMap = mutableMapOf<String, IActionHandler>()

    init {
        arrayOf(
            // Framework Info
            TestHandler, GetLatestEvents, GetSupportedActions, GetStatus, GetVersion, GetSelfInfo, GetLoginInfo,

            // UserActions
            GetProfileCard, GetFriendList, SendLike, GetUid, GetUinByUid, ScanQRCode, SetProfileCard,
            GetCookies, GetCSRF, GetCredentials, RestartMe, CleanCache, GetModelShow, SetModelShow,
            GetModelShowList, GetOnlineClients, GetStrangerInfo, IsBlackListUin,

            // GroupInfo
            GetTroopList, GetTroopInfo, GetTroopList, GetTroopMemberInfo, GetTroopMemberList,

            // GroupActions
            ModifyTroopName, LeaveTroop, KickTroopMember, BanTroopMember, SetGroupWholeBan, SetGroupAdmin,
            ModifyTroopMemberName, SetGroupUnique, GetTroopHonor, GroupPoke,

            // MSG ACTIONS
            SendMessage, DeleteMessage, GetMsg, GetForwardMsg, SendGroupForwardMsg,

            // RESOURCE ACTION
            GetRecord, GetImage,

            // WEATHER
            GetWeatherCityCode, GetWeather
        ).forEach {
            it.alias.forEach { name ->
                actionMap[name] = it
            }
            actionMap[it.path()] = it
        }
    }

    operator fun get(action: String): IActionHandler? {
        return actionMap[action]
    }
}

internal abstract class IActionHandler {
    protected abstract suspend fun internalHandle(session: ActionSession): String

    abstract fun path(): String

    open val alias: Array<String> = arrayOf()

    open val requiredParams: Array<String> = arrayOf()

    suspend fun handle(session: ActionSession): String {
        requiredParams.forEach {
            if (!session.has(it)) {
                return noParam(it, session.echo)
            }
        }
        return internalHandle(session)
    }

    protected fun ok(
        msg: String = "",
        echo: String
    ): String {
        return resultToString(true, Status.Ok, EmptyObject, msg, echo = echo)
    }

    protected inline fun <reified T> ok(data: T, echo: String, msg: String = ""): String {
        return resultToString(true, Status.Ok, data!!, msg, echo = echo)
    }

    protected fun noParam(paramName: String, echo: String): String {
        return failed(Status.BadParam, "lack of [$paramName]", echo)
    }

    protected fun badParam(why: String, echo: String): String {
        return failed(Status.BadParam, why, echo)
    }

    protected fun error(why: String, echo: String): String {
        return failed(Status.InternalHandlerError, why, echo)
    }

    protected fun logic(why: String, echo: String): String {
        return failed(Status.LogicError, why, echo)
    }

    protected fun failed(status: Status, msg: String, echo: String): String {
        return resultToString(false, status, EmptyObject, msg, echo = echo)
    }
}

internal class ActionSession {
    private val params: JsonObject
    internal val echo: String

    constructor(
        values: Map<String, Any?>,
        echo: String = ""
    ) {
        val map = hashMapOf<String, JsonElement>()
        values.forEach { (key, value) ->
            if (value != null) {
                when (value) {
                    is String -> map[key] = value.json
                    is Number -> map[key] = value.json
                    is Char -> map[key] = JsonPrimitive(value.code.toByte())
                    is Boolean -> map[key] = value.json
                    is JsonObject -> map[key] = value
                    is JsonArray -> map[key] = value
                    else -> error("unsupported type: ${value::class.java}")
                }
            }
        }
        this.echo = echo
        this.params = JsonObject(map)
    }

    constructor(
        params: JsonObject,
        echo: String = ""
    ) {
        this.echo = echo
        this.params = params
    }

    fun getLong(key: String): Long {
        return params[key].asLong
    }

    fun getLongOrNull(key: String): Long? {
        return params[key].asLongOrNull
    }

    fun getInt(key: String): Int {
        return params[key].asInt
    }

    fun getIntOrNull(key: String): Int? {
        return params[key].asIntOrNull
    }

    fun isString(key: String): Boolean {
       val element = params[key]
        return element is JsonPrimitive && element.isString
    }

    fun isArray(key: String): Boolean {
        val element = params[key]
        return element is JsonArray
    }

    fun isObject(key: String): Boolean {
        val element = params[key]
        return element is JsonObject
    }

    fun getString(key: String): String {
        return params[key].asString
    }

    fun getStringOrNull(key: String): String? {
        return params[key].asStringOrNull
    }

    fun getBoolean(key: String): Boolean {
        return params[key].asBoolean
    }

    fun <T: Boolean?> getBooleanOrDefault(key: String, default: T? = null): T {
        return (params[key].asBooleanOrNull as? T) ?: default as T
    }

    fun getObject(key: String): JsonObject {
        return params[key].asJsonObject
    }

    fun getObjectOrNull(key: String): JsonObject? {
        return params[key].asJsonObjectOrNull
    }

    fun getArray(key: String): JsonArray {
        return params[key].asJsonArray
    }

    fun getArrayOrNull(key: String): JsonArray? {
        return params[key].asJsonArrayOrNull
    }

    fun has(key: String) = params.containsKey(key)
}