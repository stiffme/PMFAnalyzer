package com.esipeng.pmf

import akka.actor.{Actor, ActorLogging}

/**
 * Created by stiffme on 2015/7/9.
 */
class PMFFileHandlerActor extends Actor with ActorLogging{
  def receive = {
    case Analyze(path,fromTime,toTime,filters) => {
      val result = PMFFile(path,fromTime,toTime,filters)
      sender ! AnalyzeResult(result)
    }
  }
}
