# Experimental
POC using a standard plugin to enable research plugins.

[Research plugins should only be enabled in nightly/snapshot builds](https://docs.scala-lang.org/scala3/reference/changed-features/compiler-plugins.html).

# Does it matter?
I don't think so. The more I dig around the more ways I find to "adjust" the compiler pipeline in arbitrary ways. It's clearly stated that research plugins are not supported in release versions of the compiler, so if you want to use this, you're not supported. If you depend on this, you're asking for your build to break.

The compiler team may not agree with this assessment - I'll leave it up to them to decide if they want to close this loophole or leave it as a known convenience workaround for which you're explicitly not supported. If things start heading down the scala 2 macros path, I'm sure it'll be closed. Use responsibly :)

I'm deliberately not publishing this to Maven Central - if you want to use it, you'll need to publish locally. I also suggest removing the code that removes half the phases from the compiler pipeline ;)

# Demo
`sbt demo`
```
[info] compiling 1 Scala source to /home/rob/code/scala3-plugin-example/demo/target/scala-3.1.2/classes ...
Hello from the standard plugin!
Scala version: version 3.1.2
Enabling experimental features
Experimental features: true
Hello from the research plugin!
Scala version: version 3.1.2
Let's do important research!
I wonder how well the compiler works with half the phases disabled...
[info] exception occurred while compiling /home/rob/code/scala3-plugin-example/demo/src/main/scala/Demo.scala
java.lang.AssertionError: assertion failed: pruneErasedDefs requires explicitOuter to be in different TreeTransformer while compiling /home/rob/code/scala3-plugin-example/demo/src/main/scala/Demo.scala
[error] ## Exception when compiling 1 sources to /home/rob/code/scala3-plugin-example/demo/target/scala-3.1.2/classes
[error] java.lang.AssertionError: assertion failed: pruneErasedDefs requires explicitOuter to be in different TreeTransformer
```
Based on this important research, we can conclude that the compiler doesn't work very well with half the phases disabled :)

# The problem
Here is [the code](https://github.com/lampepfl/dotty/blob/93eb3bb6287ecf8f5d2f37518c6e722587c6e1e9/tests/pos-with-compiler-cc/dotc/plugins/Plugins.scala#L119-L138) that adds the plugin phases to the phase plan:

```scala
        /** Add plugin phases to phase plan */
        def addPluginPhases(plan: List[List[Phase]])(using Context): List[List[Phase]] = {
            // plugin-specific options.
            // The user writes `-P:plugname:opt1,opt2`, but the plugin sees `List(opt1, opt2)`.
            def options(plugin: Plugin): List[String] = {
            def namec = plugin.name + ":"
            ctx.settings.pluginOptions.value filter (_ startsWith namec) map (_ stripPrefix namec)
            }
/*3*/       //val experimentalEnabled = Feature.isExperimentalEnabled
            // schedule plugins according to ordering constraints
/*1*/       val pluginPhases = plugins.collect { case p: StandardPlugin => p }.flatMap { plug => plug.init(options(plug)) }
            val updatedPlan = Plugins.schedule(plan, pluginPhases)

            // add research plugins
/*2*//*4*/  if (Feature.isExperimentalEnabled)  //if (experimentalEnabled)
            plugins.collect { case p: ResearchPlugin => p }.foldRight(updatedPlan) {
                (plug, plan) => plug.init(options(plug), plan)
            }
            else
            updatedPlan
        }
```

1. `StandardPlugin` phases are initialized and scheduled.
2. Check if experimental features are enabled.
   
   This ordering means a `StandardPlugin` can use reflection to enable experimental features during initialization.

   To fix:

3. Check if experimental feature should be enabled before initializing the `StandardPlugin` phases.
4. Use the stored result of the check (which can't be accessed by plugin initialization) to see if experimental features should be enabled.