package kefjs


@Deprecated("Use efhook when prepare. This method will removed in 0.8.2")
fun Ef.setOnMountListener(callback: ((ef:Ef) -> Unit)?) {
    if(callback == null) {
        this.efhook.mountFunc = { func, that ->
            kotlin.run(func)
        }
    } else {
        val hookFunc = this.efhook.mountFunc
        this.efhook.mountFunc = { func, that ->
            run {
                hookFunc(func, that)
                callback(that)
            }
        }
    }
}

@Deprecated("Use efhook when prepare. This method will removed in 0.8.2.", ReplaceWith("this.mount(root, ef)"))
fun Ef.mount_calllistener(root: String, ef: Ef?) = this.mount(root,ef)
