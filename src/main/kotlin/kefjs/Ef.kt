package kefjs

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

open class Ef {
    class EfPrepare {
        private var efprepare : dynamic
        constructor(tpl:String) {
            efprepare = js("(ef.create(tpl))")
        }
        constructor(ast: Array<Any>) {
            efprepare = js("(ef.create(ast))")
        }
        fun newInstance() : Ef {
            return Ef(efprepare)
        }
    }
    enum class EfOption(s: String) {
        REPLACE("replace"), APPEND("append")
    }
    private var instance : dynamic
    private val valueFuncMap = mutableMapOf<MethodFunction2, dynamic>()
    private val methodFuncMap = mutableMapOf<BaseMethodFunction, dynamic>()
    private val methodNameMap = mutableMapOf<String, BaseMethodFunction>()
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

        fun bundle(func: BunbleFunction) : Int{
            inform()
            var result = false
            try {
                result = func.call()
            } finally {
                return exec(result)
            }
        }

        fun onNextRender(func: MethodFunction) {
            js("ef.onNextRender(func.call)")
        }
        fun parseEft(tplast : String) = js("ef.parseEft(tplast)").unsafeCast<Array<dynamic>>()
        fun setParser(func : ParserFunction) {
            js("ef.setParser(func.call)")
        }

        //Kef
        private fun getKEf(ef : dynamic) : Ef = ef["\$k\$efjs"] as Ef
    }
    constructor(proto: EfPrepare) {
        instance = js("new proto")
        instance["\$k\$efjs"] = this
        instance["getKEf"] = js("function () { return this.\$k\$efjs }")
    }
    //Init Mount
    @Deprecated("Use EfOption instead", ReplaceWith("mount(target, EfOption.REPLACE)", "kefjs.Ef.EfOption"))
    open fun mount(target: HTMLElement?, option: String = "append") {
        mount(target, EfOption.valueOf(option))
    }
    open fun mount(target: HTMLElement?, option: EfOption = EfOption.APPEND) {
        instance.`$mount`(js("{target: target, option: option.name}"))
    }
    open fun umount() {
        instance.`$umount`()
    }

    //Subscribe
    fun subscribe(value: String, func: MethodFunction2) {
        valueFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs, option.value) }"))
        instance.`$subscribe`(value,valueFuncMap[func])
    }
    fun unsubscribe(value: String, func: MethodFunction2) {
        instance.`$unsubscribe`(value,valueFuncMap[func])
        valueFuncMap.remove(func)
    }
    //Data
    fun setData(arg: String, data: Any) {
        instance.`$data`[arg] = data
    }
    fun getData(arg: String) = instance.`$data`[arg] as Any

    //Mount
    @Deprecated("Use mount instead.", ReplaceWith("mount(root, ef)"))
    open fun subMount(root: String, ef: Ef?) {
        mount(root, ef)
    }
    open fun mount(root: String, ef: Ef?) {
        if (ef == null) {
            instance[root] = null
        } else {
            instance[root] = ef.instance
        }
    }

    fun listGet(key: String, position: Int) = instance[key][position]["\$k\$efjs"] as Ef

    fun listPush(key: String, efinstance: Ef) {
        instance[key].push(efinstance.instance)
    }
    fun listRemove(key: String, position: Int) {
        instance[key].remove(position)
    }
    fun listEmpty(key: String) {
        instance[key].empty()
    }
    fun listSize(key: String) = instance[key].length as Int

    //Refs
    fun getRefs(name:String) = instance.`$refs`[name] as HTMLElement

    //Parent
    fun getParent() = instance.`$parent`

    //Key
    fun getKey() = instance.`$key` as String

    //Element
    fun getElement() = instance.`$element` as HTMLElement

    //EFPLACEHOLDER
    fun debugEFPLACEHOLDER() = instance.__EFPLACEHOLDER__ as HTMLElement

    //Methods
    fun setMethod(name: String, func: MethodFunction1) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    fun setMethod(name: String, func: MethodFunction2) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs, option.value) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    fun setMethod(name: String, func: MethodFunction3) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs, option.value, option.e) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    fun <T> getMethod(name: String) = methodNameMap[name].unsafeCast<T>()


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
    public interface ParserFunction {
        @JsName("call")
        fun call(str: String)
    }
    interface BaseMethodFunction { }
    public interface BunbleFunction {
        @JsName("call")
        fun call() : Boolean
    }
    public interface MethodFunction : BaseMethodFunction{
        @JsName("call")
        fun call()
    }
    public interface MethodFunction1 : BaseMethodFunction{
        @JsName("call")
        fun call(state: Ef)
    }
    public interface MethodFunction2 : BaseMethodFunction{
        @JsName("call")
        fun call(state: Ef, value: String)
    }
    public interface MethodFunction3 : BaseMethodFunction{
        @JsName("call")
        fun call(state: Ef, value: String, e : Event)
    }
}

fun String.prepareEf() : Ef.EfPrepare {
    return Ef.create(this)
}
fun String.instanceEf() : Ef {
    return Ef.create(this).newInstance()
}