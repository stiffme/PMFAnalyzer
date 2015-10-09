package com.esipeng

import akka.actor.{Props, ActorSystem}
import com.esipeng.pmf.{AnalyzeFilesWithFilters, PMFFileFinderActor}
import com.github.nscala_time.time.StaticDateTimeFormat
import com.typesafe.scalalogging.Logger
import de.tototec.cmdoption.{CmdlineParserException, CmdlineParser}
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
/**
 * @author ${user.name}
 */
object MainApp{
  val log = Logger(LoggerFactory.getLogger("MainApp"))

  def main(args : Array[String]) {

    val config = new Config()
    val cp = new CmdlineParser(config)

    try{
      cp.parse(args:_*)
    } catch {
      case e:CmdlineParserException =>
        log.error(e.getLocalizedMessage)
        log.error("Run --help for help.")
        System.exit(1)
    }
    if(config.help){
      cp.usage()

    } else{
      val datePattern = StaticDateTimeFormat.forPattern("yyyyMMddHHmm")
      val fromTime = if(config.from.length == 0) None else Some(datePattern.parseDateTime(config.from))
      val toTime = if(config.to.length == 0) None else Some(datePattern.parseDateTime(config.to))

      fromTime match  {
        case None =>
        case Some(from) => {
          toTime match  {
            case None =>
            case Some(to) => {
              if(from.isBefore(to) ==  false) {
                log.error("fromTime {} should be before toTime {}!",fromTime,toTime)
                System.exit(-1)
              }
            }
          }
        }
      }
      val actorSystem = ActorSystem("Actor")
      val fileFinderActor = actorSystem.actorOf(Props[PMFFileFinderActor],"PMFFileFinderActor")
      fileFinderActor ! AnalyzeFilesWithFilters(Set.empty[String] ++ config.names.toList,fromTime,toTime,Set.empty[String] ++config.filters)
    }
  }

}
