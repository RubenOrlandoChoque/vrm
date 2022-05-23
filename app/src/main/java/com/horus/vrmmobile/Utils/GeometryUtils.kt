package com.horus.vrmmobile.Utils

import android.content.Context
import android.util.Log
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.github.bkhezry.extramaputils.builder.ExtraMarkerBuilder
import com.github.bkhezry.extramaputils.builder.ExtraPolygonBuilder
import com.github.bkhezry.extramaputils.builder.ExtraPolylineBuilder
import com.github.bkhezry.extramaputils.model.ExtraMarker
import com.github.bkhezry.extramaputils.model.ExtraPolygon
import com.github.bkhezry.extramaputils.model.ExtraPolyline
import com.github.bkhezry.mapdrawingtools.model.DrawingOption
import com.google.android.gms.maps.model.LatLng
import com.horus.vrmmobile.R
import com.horus.vrmmobile.VRMApplication
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader

object GeometryUtils {

    fun convertWKTToList(wkt: String): Pair<DrawingOption.DrawingType, ArrayList<LatLng>>?{
        var result: Pair<DrawingOption.DrawingType, ArrayList<LatLng>>? = null
        try {
            val wktr = WKTReader()
            val imageGeom = wktr.read(wkt)
            val listPoint = mutableListOf<LatLng>()
            imageGeom.coordinates.mapTo(listPoint) {
                LatLng(it.y, it.x)
            }
            val geometryType = when(imageGeom.geometryType){
                "Point" -> DrawingOption.DrawingType.POINT
                "LineString" -> DrawingOption.DrawingType.POLYLINE
                "Polygon" -> DrawingOption.DrawingType.POLYGON
                else -> DrawingOption.DrawingType.POINT
            }
            val arrayListPoint = ArrayList<LatLng>()
            arrayListPoint.addAll(listPoint)
            result = Pair(geometryType, arrayListPoint)
        }catch (e: Exception){
            Log.e("convertWKTToList", e.message)
        }
        return result
    }

    fun convertListToWKT(points: ArrayList<LatLng>, geometryType: DrawingOption.DrawingType): String{
        var result = ""
        try {
            if(points.size == 0) return result
            val fact = GeometryFactory()
            val listCoordenates = mutableListOf<Coordinate>()
            points.mapTo(listCoordenates){ Coordinate(it.longitude, it.latitude) }
            when(geometryType){
                DrawingOption.DrawingType.POINT -> {
                    val point = fact.createPoint(listCoordenates[0])
                    result = point.toText()
                }
                DrawingOption.DrawingType.POLYLINE -> {
                    val polyline = fact.createLineString(listCoordenates.toTypedArray())
                    result = polyline.toText()
                }
                DrawingOption.DrawingType.POLYGON -> {
                    val polygon = fact.createPolygon(listCoordenates.toTypedArray())
                    result = polygon.toText()
                }
            }
        }catch (e: Exception){
            Log.e("convertListToWKT", e.message)
        }
        return result
    }

    fun getPoint(point: LatLng): ExtraMarker {
        return ExtraMarkerBuilder()
                .setCenter(point)
                .setIcon(R.drawable.ic_location as @IdRes Int)
                .build()
    }

    fun getPolyline(points: Array<LatLng>, context: Context): ExtraPolyline {
        return ExtraPolylineBuilder()
                .setPoints(points)
                .setzIndex(0f)
                .setStrokeWidth(Utils.convertDpToPx(context, 3))
                .setStrokeColor(ContextCompat.getColor(context, R.color.orange))
                .build()
    }

    fun getPolygon(points: Array<LatLng>, context: Context): ExtraPolygon {
        return ExtraPolygonBuilder()
                .setPoints(points)
                .setzIndex(0f)
                .setStrokeWidth(Utils.convertDpToPx(context, 3))
                .setStrokeColor(ContextCompat.getColor(context, R.color.orange))
                .setFillColor(ContextCompat.getColor(context, R.color.orange_fill))
                .build()
    }
}