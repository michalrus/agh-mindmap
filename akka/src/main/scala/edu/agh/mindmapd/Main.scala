package edu.agh.mindmapd

object Main extends App {

  akka.Main main Array(classOf[edu.agh.mindmapd.actors.Supervisor].getCanonicalName)

}
