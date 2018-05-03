package kefjs



fun Ef.setOnMountListener(callback: ((ef:Ef) -> Unit)?) {
    this.editUserStore("onMountListener_\$kefexp", callback)
}

fun Ef.mount_calllistener(root: String, ef: Ef?) {
    if (Ef.infoLevel > 0) if(Ef.isPaused()) {
        console.warn("ef.js render is paused! Check your code!")
    }
    this.mount(root, ef)
    ef!!.getUserStore("onMountListener_\$kefexp",  { _: Ef ->
            if(Ef.infoLevel > 0) console.warn("Nothing to call")
    })(ef!!)
}

