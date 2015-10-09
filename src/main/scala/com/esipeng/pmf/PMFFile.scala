package com.esipeng.pmf

import java.io.Reader
import javax.xml.parsers.SAXParserFactory

import com.github.nscala_time.time.StaticDateTimeFormat
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.xml._

/**
 * Created by stiffme on 2015/7/9.
 */
class PMFFile(val dateFrom:DateTime,val dateTo:DateTime,val fileName:String, val filters:Set[String]) {
  val log = Logger(LoggerFactory.getLogger(classOf[PMFFile]))
  val factory = SAXParserFactory.newInstance();

  factory.setValidating(false)
  factory.setFeature("http://xml.org/sax/features/namespaces", false)
  factory.setFeature("http://xml.org/sax/features/validation", false)
  factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
  factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

  val fileContent = XML.withSAXParser(factory.newSAXParser()).load(fileName)
  private val cacheMap = (fileContent \\ "mi").foldLeft(collection.mutable.Map.empty[String,PMFData]) {
    (miMap,miElement) =>  {
      var count:Double = 0
      val miName = (miElement \ "mt").text.toString

      var filterInclude = false
      if(filters.size != 0) {
        for( f <- filters)  {
          if(filterInclude == false)  {
            val filterReg = f.r
            miName match {
              case filterReg(_*) =>
                filterInclude = true
              case _ =>
                filterInclude = false
            }
          }
        }
      } else
        filterInclude = true

      if(filterInclude == true) {
        //log.debug(s"filename: $fileName. type: $miName")
        val mi = (miElement \ "mv").foldLeft(collection.mutable.Map.empty[String,PMFValue])  {
          (mvMap:collection.mutable.Map[String,PMFValue],mvElement) =>  {
            val moid:String = (mvElement \ "moid").text.toString
            val rV = (mvElement \ "r")
            if(rV.size == 1)  {
              val r = (mvElement \ "r").text.toString.toDouble
              if(r != 0)  {
                val pmfValue:PMFValue = PMFValue(moid,r)
                count += r
                log.debug(s"file:$fileName type: $miName moid: $moid r: $r count: $count")
                mvMap += (moid -> pmfValue)
              }
            }
            mvMap
          }
        }

        if(count != 0)  {
          val pmfData = PMFData(miName,mi.toMap,count)
          miMap += (miName -> pmfData)
        }

      }
      miMap
    }
  }

  def getCount(mtName:String):Double = {
    val pmfData = cacheMap.get(mtName)
    pmfData match {
      case Some(x:PMFData) => x.count
      case None => 0
    }
  }

  def mtTypes() = cacheMap.keySet
}


object PMFFile  {
  val log = Logger(LoggerFactory.getLogger(classOf[PMFFile]))
  val filenamePattern = """.*A(\d{8})\.(\d{4})-(\d{4})_.*""".r
  val datePattern = StaticDateTimeFormat.forPattern("yyyyMMddHHmm")
  def apply(filename:String,fromOpt:Option[DateTime],toOpt:Option[DateTime],filters:Set[String]):Option[PMFFile] = {
    filename match {
      case filenamePattern(main,date1,date2) => {
        try{
          val dateFrom = datePattern.parseDateTime(main + date1)
          val dateTo = datePattern.parseDateTime(main + date2)
          var handleFlag = true

          fromOpt match {
            case Some(from:DateTime) =>
              handleFlag = dateFrom.isAfter(from)
            case None =>
          }
          if(handleFlag == true)  {
            toOpt match {
              case Some(to:DateTime) =>
                handleFlag = dateTo.isBefore(to)
              case None =>
            }
          }


          if(handleFlag == true)  {
            log.debug(s"Successfully added $filename")
            Some(new PMFFile(dateFrom, dateTo, filename,filters))
          } else {
            log.debug("{} is ignored due to time",filename)
            None
          }


        } catch {
          case e: Exception =>
            log.warn("Error handling {}", filename, e)
            None
        }
      }

      case _ => {
        log.warn(s"Failed added $filename")
        None
      }
    }
  }
}