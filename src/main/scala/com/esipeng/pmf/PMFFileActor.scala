package com.esipeng.pmf

import akka.actor.{Props, ActorLogging, Actor}
import akka.routing.BalancingPool
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.StringBuilder
import scala.collection.mutable

/**
 * Created by stiffme on 2015/7/9.
 */
class PMFFileActor extends Actor with ActorLogging{
  //====================test========================
  //val handler = context.actorOf(Props[PMFFileHandlerActor])

  val router = context.actorOf(BalancingPool(4).props(Props[PMFFileHandlerActor]),"router")
  //================================================
  def receive = {
    case AnalyzeFilesWithFilters(files,fromTime,toTime,filters) => {
      log.debug("Receive signal AnalyzeFiles")
      val aggretor = context.actorOf(Aggretor.props(files.size),"aggretor")
      files.foreach { file =>
        log.debug("sending file to handler: {}",file)
        router.tell(Analyze(file,fromTime,toTime,filters),aggretor)
      }
    }
    case _ => {
      log.info("Receive unknown signal")
      context.system.shutdown()
    }
  }
}


class Aggretor(expected:Int) extends Actor with ActorLogging  {
  val cvsoutput = Logger(LoggerFactory.getLogger("cvsoutput"))
  var count:Int = 0
  val aggreResult =  mutable.Map[DateTime, mutable.Map[String,Double] ] ()
  val types = mutable.SortedSet[String] ()

  private def printResult: Unit = {
    val sb = new StringBuilder()
    //print type first
    sb ++= ","
    sb ++= types.mkString( ",")
    sb ++= "\n"

    val sortedTime = aggreResult.keySet.toList.sortWith(_.isBefore(_))
    sortedTime.foreach( time => {
      val currentDataOpt = aggreResult.get(time)
      currentDataOpt match {
        case None => log.error("Internal error occured...")
        case Some(currentData:mutable.Map[String,Double]) =>  {
          sb ++= time.toString
          sb ++= ","
          types.foreach( t => {
            val v:Double = currentData.getOrElse(t,0)
            sb ++= "%1.0f" format v
            sb ++= ","
          })
        }
      }
      sb ++= "\n"
    })
    cvsoutput.info(sb.toString)
    log.info("output.csv is generated.")
    context.system.shutdown()
  }

  def receive = {
    case AnalyzeResult(pmfFileOpt) =>  {
      pmfFileOpt match  {
        case None => count+=1
          if(count == expected) printResult
        case Some(pmfFile:PMFFile)  =>  {
          count += 1
          val valueMap = aggreResult.getOrElseUpdate(pmfFile.dateFrom,mutable.Map[String,Double]())
          for( mtType <- pmfFile.mtTypes()) {
            types += mtType
            val currentValue:Double = valueMap.getOrElse(mtType,0)
            valueMap.put(mtType,currentValue + pmfFile.getCount(mtType))
          }
          if(count == expected) printResult
        }
      }
    }
  }
}

object Aggretor {
  def props(expected:Int) = Props(new Aggretor(expected))
}