package experimental

import java.lang.reflect.Field

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Phases.Phase
import dotty.tools.dotc.config.Properties
import dotty.tools.dotc.plugins._

class SuperTopSecretResearchPlugin extends StandardPlugin with ResearchPlugin {
  val name: String = "research plugin"
  override val description: String = "self-enabling research plugin"

  // standard plugin initialization
  def init(options: List[String]): List[PluginPhase] = {
    // oopsies
    println("Hello from the standard plugin!")
    println(s"Scala version: ${Properties.versionString}")
    println("Enabling experimental features")
    val experimental: Field =
      Properties.getClass().getDeclaredField("experimental")
    experimental.setAccessible(true)
    experimental.set(Properties, true)
    println(s"Experimental features: ${Properties.experimental}")
    Nil
  }

  // research plugin initialization
  def init(options: List[String], phases: List[List[Phase]])(implicit
      ctx: Context
  ): List[List[Phase]] = {
    println("Hello from the research plugin!")
    println(s"Scala version: ${Properties.versionString}")
    println(
      "Let's do important research!\nI wonder how well the compiler works with half the phases disabled..."
    )
    phases.zipWithIndex.filter(_._2 % 2 == 0).map(_._1)
  }
}
