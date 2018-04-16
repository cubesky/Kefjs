package kefjs

fun Ef.setOnMountListener(callback: OnMountListener?) {
    this.editUserStore("onMountListener_\$kefexp", callback)
}

fun Ef.mount_calllistener(root: String, ef: Ef?) {
    this.mount(root, ef)
    ef!!.getUserStore("onMountListener_\$kefexp", object : OnMountListener {
        override fun onMount(ef: Ef) {
            println("Nothing to call")
        }
    }).onMount(ef)
}

interface OnMountListener {
    fun onMount(ef: Ef)
}
