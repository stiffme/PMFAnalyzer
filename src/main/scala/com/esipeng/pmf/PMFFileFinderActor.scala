package com.esipeng.pmf

import java.io.File

import akka.actor.{Props, Actor, ActorLogging}

/**
 * Created by stiffme on 2015/7/10.
 */
class PMFFileFinderActor extends Actor with ActorLogging {
  val pmfFileActor = context.actorOf(Props[PMFFileActor],"PMFFileActor")

  def receive = {
    case AnalyzeFilesWithFilters(searchPath,fromTime,toTime,filters) => {
      val concretePaths = findFiles(searchPath )
      concretePaths.foreach( log.debug(_) )
      if(concretePaths.size == 0) {
        log.error("No files found in given search locations.")
        context.system.shutdown()
      } else{
        log.info("Processing {} files..",concretePaths.size)
        pmfFileActor ! AnalyzeFilesWithFilters(concretePaths,fromTime,toTime,filters)
      }

    }
  }

  private def findFiles(searchPath:Set[String]):Set[String] = {
    val ret = collection.mutable.Set[String]()
    searchPath.foreach( sp => {
      val file = new File(sp)
      if(file.exists()) {
        if(file.isFile) ret += sp
        else ret ++= findSinglePath(file)
      }
    })
    ret.toSet
  }

  private def findSinglePath(file: File):Set[String] = {
    val ret = collection.mutable.Set[String]()
    val subFiles = file.listFiles()
    subFiles.foreach( f => {
      if(f.isFile) ret += f.getPath
      else ret ++= findSinglePath(f)
    })

    ret.toSet
  }
}
