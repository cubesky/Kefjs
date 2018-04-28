package kefjs



fun Ef.setOnMountListener(callback: ((ef:Ef) -> Unit)?) {
    this.editUserStore("onMountListener_\$kefexp", callback)
}

fun Ef.mount_calllistener(root: String, ef: Ef?) {
    if (Ef.infoLevel > 0) when(Ef.status()) {
        EfStatus.PAUSED -> console.warn("ef.js render is paused! Check your code!")
        EfStatus.PANIC -> console.error("What?! ef.js panic! Ouch!")
        EfStatus.RUNNING -> { /* Nothing happened */ }
    }
    this.mount(root, ef)
    ef!!.getUserStore("onMountListener_\$kefexp",  { _: Ef ->
            if(Ef.infoLevel > 0) console.warn("Nothing to call")
    })(ef!!)
}

fun Ef.Companion.status() : EfStatus {
    Ef.inform()
    val execReturn = Ef.exec()
    return if (execReturn > 0) EfStatus.PAUSED else if (execReturn == 0) EfStatus.RUNNING else EfStatus.PANIC
}

enum class EfStatus {
    RUNNING, PAUSED, PANIC
}

