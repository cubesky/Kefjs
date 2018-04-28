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
    private val method = mutableListOf<KefMethodModel>()
    infix fun String.bind(func: (state: Ef, value: String, e: Event) -> Unit) {
        method += KefMethodModel(this, func)
    }
    fun build() = method
}

@KefConfig
class KefConfigBuilder {
    private val data = mutableListOf<KefDataModel>()
    private val method = mutableListOf<KefMethodModel>()
    private val mount = mutableMapOf<String,Ef>()
    private var onMountListener : ((ef: Ef) -> Unit)? = null
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
        method.addAll(methods)
    }

    fun onMount(callback : (ef: Ef)->Unit) {
        this.onMountListener = callback
    }

    fun build(): KefConfigModel = KefConfigModel(this.data, this.method, mount, this.onMountListener)
}

fun kefconfig(setup: KefConfigBuilder.() -> Unit): KefConfigModel = KefConfigBuilder().apply { this.setup() }.build()


data class KefDataModel(val arg: String, val data: Any)
data class KefMethodModel(val name: String, val func: (state:Ef, value: String, e: Event) -> Unit)
data class KefConfigModel(val data: MutableList<KefDataModel>,
                          val method: MutableList<KefMethodModel>,
                          val mount: MutableMap<String, Ef>,
                          val onMount: ((ef: Ef)->Unit)?)