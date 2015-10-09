package com.esipeng.pmf

/**
 * Created by stiffme on 2015/7/9.
 */

case class PMFValue(moid:String,countValue:Double)

case class PMFData(mtName:String,mtValues:Map[String,PMFValue],count:Double) {

}

