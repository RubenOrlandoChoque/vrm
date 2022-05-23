package com.horus.vrmmobile.Models.Dtos

class ItemEncuestaSyncByFormulario {

    var nombreFormulario = ""
    var codFormulario = ""
    var codVersion = ""
    var isSeleccionado = false
    var estado = 0

    var encuestas = 0
    var novedades = 0
    var puntosInteres = 0
    var archivos = 0

    var encuestas_sync = 0
    var novedades_sync = 0
    var puntosInteres_sync = 0
    var archivos_sync = 0

    var isEncuesta_error = false
    var isNovedades_error = false
    var isPuntoInteres_error = false
    var isArchivos_error = false

    var isSyncEncuestas = false
    var isSyncArchivos = false
    var isSyncronizando = false
}