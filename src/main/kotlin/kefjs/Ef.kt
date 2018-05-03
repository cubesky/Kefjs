package kefjs

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

class Ef : IEF{
    class EfPrepare {
        private var efprepare : dynamic
        constructor(tpl:String) {
            efprepare = js("(ef.create(tpl))")
        }
        constructor(ast: Array<Any>) {
            efprepare = js("(ef.create(ast))")
        }
        fun newInstance(config:KefConfigModel = kefconfig {  }): Ef = Ef(efprepare, config)
    }
    enum class EfOption(s: String) {
        REPLACE("replace"), APPEND("append"), BEFORE("before"), AFTER("after")
    }
    private var instance : dynamic
    private val valueFuncMap = mutableMapOf<(state:Ef, value:String, e:Event)->Unit, dynamic>()
    private val methodFuncMap = mutableMapOf<(state:Ef, value:String, e:Event)->Unit, dynamic>()
    private val methodNameMap = mutableMapOf<String, (state:Ef, value:String, e:Event)->Unit>()
    companion object {
        /*
         0 for production
         1 for debug
         */
        var infoLevel = 1

        fun create(tpl:String) = EfPrepare(tpl)

        fun create(ast:Array<Any>) = EfPrepare(ast)

        fun inform() = js("ef.inform()") as Int

        fun exec(force : Boolean = false) = js("ef.exec(force)") as Int

        fun bundle(func: () -> Boolean) : Int{
            inform()
            var result = false
            try {
                result = func()
            } finally {
                return exec(result)
            }
        }

        fun isPaused() = js("ef.isPaused") as Boolean

        fun onNextRender(func: ()->Unit) {
            js("ef.onNextRender(func)")
        }

        fun parseEft(tplast : String) = js("ef.parseEft(tplast)").unsafeCast<Array<dynamic>>()

        fun setParser(func: (str: String)->Unit) {
            js("ef.setParser(func)")
        }

        //Kef
        private fun getKEf(ef : dynamic) : Ef = ef["\$k\$efjs"] as Ef

        //Utils
        fun createFunc(func: (state: Ef, value: String, e: Event) -> Unit) = func
    }
    private constructor(proto: EfPrepare, config:KefConfigModel) {
        instance = js("new proto")
        instance["\$k\$efjs"] = this
        instance["getKEf"] = js("function () { return this.\$k\$efjs }")
        data = KefData(instance)
        methods = KefMethod(this)
        ctx = KefCtx(instance)
        element = ctx.nodeInfo.element
        Ef.bundle {
            config.data.forEach {
                data[it.arg] = it.data
            }
            config.method.forEach {
                setMethod(it.name, it.func)
            }
            this.setOnMountListener(config.onMount)
            val isDebug = Ef.infoLevel
            Ef.infoLevel = 0 //Temporarily disable Ef status check
            config.mount.forEach {
                mount_calllistener(it.key, it.value)
            }
            Ef.infoLevel = isDebug //Restore Ef status check
            false
        }
    }
    //Init Mount
    fun mount(target: HTMLElement?, option: EfOption = EfOption.APPEND) {
        val option_str = when(option) {
            EfOption.APPEND -> "append"
            EfOption.REPLACE -> "replace"
            EfOption.AFTER -> "after"
            EfOption.BEFORE -> "before"
        }
        instance.`$mount`(js("{target: target, option: option_str}"))
    }
    fun mount(target: HTMLElement?) {
        mount(target, EfOption.APPEND)
    }
    fun umount() {
        instance.`$umount`()
    }

    //Context
    public val ctx : KefCtx
    public val element : HTMLElement

    //Subscribe
    fun subscribe(name: String, func: (state: Ef, value: String, e: Event)-> Unit) {
        valueFuncMap.put(func, js("function (option) { func(option.state.\$k\$efjs, option.value, '') }"))
        instance.`$subscribe`(name,valueFuncMap[func])
    }
    fun unsubscribe(name: String, func: (state: Ef, value: String, e: Event)-> Unit) {
        instance.`$unsubscribe`(name,valueFuncMap[func])
        valueFuncMap.remove(func)
    }
    //Data
    public val data : KefData

    //Mount
    override fun mount(root: String, ef: Ef?) {
        if (ef == null) {
            instance[root] = null
        } else {
            instance[root] = ef.instance
        }
    }

    fun list(key: String) = KefList(key, instance)


    //Refs
    fun getRefs(name:String) = instance.`$refs`[name] as HTMLElement


    //Methods
    var methods : KefMethod
    private fun setMethod(name: String, func: (state: Ef, value: String, e: Event) -> Unit) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func(option.state.\$k\$efjs, option.value, option.e) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    private fun getMethod(name: String) = methodNameMap[name] as (state: Ef, value: String, e: Event) -> Unit


    //Store
    private val userStoreMap = mutableMapOf<String, Any>()
    fun editUserStore(key: String, value: Any?) {
        if(value == null) {
            userStoreMap.remove(key)
        } else {
            userStoreMap.put(key, value)
        }
    }
    fun <T> getUserStore(key: String, default: T) : T = if (userStoreMap[key] == null) default else userStoreMap[key].unsafeCast<T>()

    //Raw
    fun getInstance() = instance

    //Helper
    class KefList(val key: String, val instance: dynamic) {
        operator fun get(position: Int): Ef? = instance[key][position]["\$k\$efjs"] as Ef
        fun push(efinstance: Ef) = instance[key].push(efinstance.instance) as Int
        fun push(vararg efinstances: Ef) : Int {
            Ef.bundle({
                efinstances.forEach { push(it) }
                false
            })
            return size()
        }

        fun remove(position: Int) {
            instance[key].remove(position)
        }
        fun empty() {
            instance[key].empty()
        }
        fun size() = instance[key].length as Int
    }

    class KefMethod(val ef: Ef) {
        operator fun get(name: String) = ef.getMethod(name)
        operator fun set(name: String, func: (state: Ef, value: String, e:Event) -> Unit) = ef.setMethod(name, func)
    }

    class KefData(val instance: dynamic) {
        operator fun get(arg: String) = instance.`$data`[arg] as Any
        operator fun <T> get(arg: String) = instance.`$data`[arg].unsafeCast<T>()
        operator fun set(arg: String, data:Any) {
            instance.`$data`[arg] = data
        }
    }

    class KefCtx(instance: dynamic) {
        var nodeInfo = KefCtxNodeInfo(instance)
        class KefCtxNodeInfo(val instance: dynamic) {
            var element = instance.element as HTMLElement
        }
    }

}

interface IEF {
    fun mount(root: String, ef: Ef?)
}

fun String.prepareEf() : Ef.EfPrepare = Ef.create(this)
fun String.instanceEf(config:KefConfigModel = kefconfig {  }) : Ef = this.prepareEf().newInstance(config)

