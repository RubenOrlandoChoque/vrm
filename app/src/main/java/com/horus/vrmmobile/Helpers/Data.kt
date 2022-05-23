package com.horus.vrmmobile.Helpers

object Data {
    var listaerrores: String = ""
    var sincronizado: Int = 0
    var errores: Int = 0
    var total_registros: Int = 0
    var archivo_total: Int = 0
    var archivo_sincronizado: Int = 0
    var archivo_error: Int = 0

    fun cleanContadores() {
        sincronizado = 0
        errores = 0
        listaerrores = ""
        total_registros = 0
        archivo_total = 0
        archivo_sincronizado = 0
        archivo_error = 0
    }
}