package com.vogonjeltz.weather.lib

import ucar.ma2.Index

import scala.collection.mutable.ArrayBuffer

//TODO: Work on grid filters
/*
They need to be able to "change" the grid spec for example flip lat/long

CURRENTLY: The grid spec is the ideal spec and then the filters allow actual access to this
 - Is this robust enough? - if filters are wrong you will get IOOB exceptions etc

 */

class GridFilter(val gridSpec: GridSpec) {

  import gridSpec._

  lazy val composeList: ArrayBuffer[GridFilter] = ArrayBuffer()
  registerFilter(this)

  def convertToIDX(lat: Double, long: Double): (Int, Int) = {

    assert( long >= minLong && long <= maxLong )
    assert( lat >= minLat && lat <= maxLat )

    //TODO: Verify correct rounding strategy
    val longIDX = ((long - minLong) / longRes).floor.toInt
    val latIDX = ((lat - minLat) / latRes).floor.toInt

    (latIDX, longIDX)

  }

  protected def applyFilterIDX(index: (Int, Int)): (Int, Int) = index

  final def filterIDX(index: (Int, Int)): (Int, Int) = {

    var idx = index

    for (filter <- composeList) {
      idx = filter.applyFilterIDX(idx)
    }

    idx

  }

  final def registerFilter(filter: GridFilter): Unit = composeList.append(filter)

}

class UcarGridFilter(spec: GridSpec, val idxMapper: (Index) => (Int, Int) => Index) extends GridFilter(spec) {

}

/*
NOTE: ORDER DOES MATTER
 */
class FlipLatLongFilter(spec: GridSpec) extends GridFilter(spec) {

  override def applyFilterIDX(index: (Int, Int)): (Int, Int) = (index._2, index._1)

}

class FlipLatFilter(spec: GridSpec) extends GridFilter(spec) {

  override def applyFilterIDX(index: (Int, Int)): (Int, Int) = (gridSpec.latSize - index._1 - 1, index._2)

}

class FlipLongFilter(spec: GridSpec) extends GridFilter(spec) {

  override def applyFilterIDX(index: (Int, Int)): (Int, Int) = (index._1, gridSpec.longSize - index._2 - 1)

}