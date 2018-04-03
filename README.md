# Kefjs
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://raw.githubusercontent.com/cubesky/Kefjs/master/LICENSE)

A Kotlin/JS Wrapper for [ef.js](https://ef.js.org).

Related project:
  * [ef.js](https://github.com/TheNeuronProject/ef.js)

## Usage
Import Kefjs from [CubeSky Repo](https://cubesky-mvn.github.io/) with Maven or Gradle.

```kotlin
Ef.setParser(ParserMethod) //Change the default parser for ef.js so you can use a different type of template
Ef.parseEft('Your awesome template') //Get ef.js ast using default parser

val templateString = "Your awesome template"
val ast = arrayOf(/* AST Which Support by ef */)

val template1 = Ef.create(template)
val template2 = Ef.create(ast)
val template3 = """
Your awesome template
""".prepareEf()

val component1 = template1.newInstance()
val component3 = template3.newInstance().apply {
  this.setData(key, value) // same as ef.js component1.$data.key = value
  this.setMethod(methodName, MethodFunction1) // same as ef.js component1.$methods.key = function ({state}) {}
  this.setMethod(methodName, MethodFunction2) // same as ef.js component1.$methods.key = function ({state, value}) {}
  this.setMethod(methodName, MethodFunction3) // same as ef.js component1.$methods.key = function ({state, value, e}) {}
}

Ef.onNextRender(MethodFunction) // Cache operations to execute on next render
Ef.inform() // Tell ef to cache operations **USE WITH CARE**
Ef.exec() // Tell ef to execute all cached operations **USE WITH CARE**
Ef.exec(true) // Force execute cached operations **USE WITH CARE**
Ef.bundle(MethodFunction) // Wrapper for Ef.inform() and Ef.exec()

component1.getElement() // The DOM element of component1
component2.getElement() // The DOM element of component2

component1.setData("something", "Something new") // Update Binding Data
component2.setMethod("someMethod", object : MethodFunction3 {
  override fun call(state: Ef.EfInstance, value: String, e: Event) {
    val efstate = state.getKEf() // Transform EfInstance to Ef
    efstate.setData("something", "Something new")
    println("Event target ${e.target}")
    println("Value passed $value")
  }
})

val logData = object: MethodFunction2 {
  override fun call(state: Ef.EfInstance, value: String) {
    println("Subscribed data updated: $value")
  }
}
component1.subscribe("info.data", logData) // Observe a value
component1.unsubscribe("info.data", logData) // Stop observing a value

component2.getParent() // Get where the component is mounted

component2.getRefs("example") // Get referenced nodes named 'example'

component1.subMount("mountingPoint",component2) // Mount component2 to 'mountingPoint' on component1
component1.subMount("mountingPoint",null) // Detach the mounted component

component1.listPush("listMP", component2) // Mount component2 to list 'listMP' mounting point on component1

component1.mount(target, option) // Mount method called by ef when trying to mount
component1.umount() // Unmount from parent
```

## LICENSE
[MIT](https://raw.githubusercontent.com/cubesky/Kefjs/master/LICENSE)