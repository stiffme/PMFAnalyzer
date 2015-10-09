package com.esipeng.pmf

import org.joda.time.DateTime

/**
 * Created by stiffme on 2015/7/9.
 */
case class Analyze(path:String,fromOpt:Option[DateTime],toOpt:Option[DateTime],filters:Set[String])
case class AnalyzeResult(result:Option[PMFFile])

//case class AnalyzeFiles(paths:Set[String],fromOpt:Option[DateTime],toOpt:Option[DateTime])

case class AnalyzeFilesWithFilters(paths:Set[String],fromOpt:Option[DateTime],toOpt:Option[DateTime],filters:Set[String])