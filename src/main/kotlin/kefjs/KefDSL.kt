package kefjs

import org.w3c.dom.events.Event

@DslMarker
annotation class KefConfig

@KefConfig
class KefDataBuilder {
    private val data = mutableListOf<KefDataModel>()
    infix fun String.setTo(setdata: Any) {
        data += KefDataModel(this, setdata)
    }
    fun build() = data
}

@KefConfig
class KefMountBuilder {
    private val mount = mutableMapOf<String,Ef>()
    infix fun Ef.mountTo(mountpoint: String) {
        mount.put(mountpoint, this)
    }
    fun build() = mount
}

@KefConfig
class KefMethodBuilder {
    private val method1 = mutableListOf<KefMethod1Model>()
    private val method2 = mutableListOf<KefMethod2Model>()
    private val method3 = mutableListOf<KefMethod3Model>()
    infix fun String.bind(func: (state: Ef) -> Unit) {
        method1 += KefMethod1Model(this, func)
    }
    infix fun String.bind(func: (state: Ef, value: String) -> Unit) {
        method2 += KefMethod2Model(this, func)
    }
    infix fun String.bind(func: (state: Ef, value: String, e: Event) -> Unit) {
        method3 += KefMethod3Model(this, func)
    }
    fun build() = KefMethodModel(method1,method2,method3)
}

@KefConfig
class KefConfigBuilder {
    private val data = mutableListOf<KefDataModel>()
    private val method1 = mutableListOf<KefMethod1Model>()
    private val method2 = mutableListOf<KefMethod2Model>()
    private val method3 = mutableListOf<KefMethod3Model>()
    private val mount = mutableMapOf<String,Ef>()
    private var onMountListener : OnMountListener? = null
    fun data(setup: KefDataBuilder.() -> Unit) {
        val dataBuilder = KefDataBuilder()
        dataBuilder.setup()
        data.addAll(dataBuilder.build())
    }
    fun mount(setup: KefMountBuilder.() -> Unit) {
        val mountBuilder = KefMountBuilder()
        mountBuilder.setup()
        mount.putAll(mountBuilder.build())
    }
    fun methods(setup: KefMethodBuilder.() -> Unit) {
        val methodBuilder = KefMethodBuilder()
        methodBuilder.setup()
        val methods = methodBuilder.build()
        method1.addAll(methods.method1)
        method2.addAll(methods.method2)
        method3.addAll(methods.method3)
    }

    fun onMount(callback : (ef: Ef)->Unit) {
        onMountListener = object : OnMountListener {
            override fun onMount(ef: Ef) {
                callback(ef)
            }
        }
    }

    fun build(): KefConfigModel = KefConfigModel(this.data, this.method1, this.method2, this.method3, mount, this.onMountListener)
}

fun kefconfig(setup: KefConfigBuilder.() -> Unit): KefConfigModel = KefConfigBuilder().apply { this.setup() }.build()


data class KefDataModel(val arg: String, val data: Any)
data class KefMethodModel(val method1: MutableList<KefMethod1Model>, val method2: MutableList<KefMethod2Model>, val method3: MutableList<KefMethod3Model>)
data class KefMethod1Model(val name: String, val func: (state:Ef) -> Unit)
data class KefMethod2Model(val name: String, val func: (state:Ef, value: String) -> Unit)
data class KefMethod3Model(val name: String, val func: (state:Ef, value: String, e: Event) -> Unit)
data class KefConfigModel(val data: MutableList<KefDataModel>,
                          val method1: MutableList<KefMethod1Model>,
                          val method2: MutableList<KefMethod2Model>,
                          val method3: MutableList<KefMethod3Model>,
                          val mount: MutableMap<String, Ef>,
                          val onMount: OnMountListener?)