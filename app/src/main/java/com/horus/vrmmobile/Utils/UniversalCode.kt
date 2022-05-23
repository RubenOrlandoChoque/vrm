package com.horus.vrmmobile.Utils

import android.util.Log
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.ParseException
import java.util.*

object UniversalCode {

    fun calcularCodPersona(primerNombre: String, segundoNombre: String, primerApellido: String, segundoApellido: String, fechaNac: String): String {
        var primerNombre = primerNombre
        var segundoNombre = segundoNombre
        var primerApellido = primerApellido
        var segundoApellido = segundoApellido
        var fechaNac = fechaNac
        if (fechaNac.contains("-")) {
            try {
                fechaNac = DateUtils.convertToShortFormatSlash(fechaNac)
            } catch (e: ParseException) {
                Log.e("calcularCodPersona", e.message)
            }
        }
        var regresaCodPersona = ""
        primerNombre = primerNombre.trim()
        segundoNombre = segundoNombre.trim()
        primerApellido = primerApellido.trim()
        segundoApellido = segundoApellido.trim()
        primerNombre = primerNombre.replace('ñ', 'n').replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u').replace('Ñ', 'N').replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U').replace('Ü', 'U')
        segundoNombre = segundoNombre.replace('ñ', 'n').replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u').replace('Ñ', 'N').replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U').replace('Ü', 'U')
        primerApellido = primerApellido.replace('ñ', 'n').replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u').replace('Ñ', 'N').replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U').replace('Ü', 'U')
        segundoApellido = segundoApellido.replace('ñ', 'n').replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u').replace('Ñ', 'N').replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U').replace('Ü', 'U')
        if (primerNombre.length > 30) {
            primerNombre = primerNombre.substring(0, 30)
        }
        if (segundoNombre.length > 30) {
            segundoNombre = segundoNombre.substring(0, 30)
        }
        if (primerApellido.length > 30) {
            primerApellido = primerApellido.substring(0, 30)
        }
        if (segundoApellido.length > 30) {
            segundoApellido = segundoApellido.substring(0, 30)
        }

        fechaNac = fechaNac.trim()
        if (primerNombre.isEmpty()) {
            return "Falta el Primer Nombre"
        }
        if (primerApellido.isEmpty() && segundoApellido.isEmpty()) {
            return "Para el correcto Calculo de RFC, Cuando menos debe tener un Apellido"
        }
        if (primerApellido.isEmpty() && !segundoApellido.isEmpty()) {
            primerApellido = segundoApellido
            segundoApellido = ""
        }
        if (fechaNac.isEmpty()) {
            return "Falta la Fecha de Nacimiento"
        }
        primerNombre = primerNombre.replace("0", "a").replace("1", "b").replace("2", "c").replace("3", "d").replace("4", "e").replace("5", "f").replace("6", "g").replace("7", "h").replace("8", "i").replace("9", "j").replace("'", "")
        segundoNombre = segundoNombre.replace("0", "a").replace("1", "b").replace("2", "c").replace("3", "d").replace("4", "e").replace("5", "f").replace("6", "g").replace("7", "h").replace("8", "i").replace("9", "j").replace("'", "")
        primerApellido = primerApellido.replace("0", "a").replace("1", "b").replace("2", "c").replace("3", "d").replace("4", "e").replace("5", "f").replace("6", "g").replace("7", "h").replace("8", "i").replace("9", "j").replace("'", "")
        segundoApellido = segundoApellido.replace("0", "a").replace("1", "b").replace("2", "c").replace("3", "d").replace("4", "e").replace("5", "f").replace("6", "g").replace("7", "h").replace("8", "i").replace("9", "j").replace("'", "")

//        var fechaNacimiento = 0
//        try {
//            val start = DateUtils.toShortFormatSlash(fechaNac)
//            fechaNacimiento = fechaJuliana(start!!)
//        } catch (e: ParseException) {
//            Log.e("calcularCodPersona", e.message)
//        }

        primerNombre = "$primerNombre $segundoNombre"

        var grdprimape = ""
        var grdsegape = ""
        var nombreCompleto = ""

        grdprimape = primerApellido
        grdsegape = segundoApellido

        grdprimape = CodPersona_QuitaEspacios(grdprimape)
        grdsegape = CodPersona_QuitaEspacios(grdsegape)

        nombreCompleto = grdprimape.trim { it <= ' ' } + " " + grdsegape.trim { it <= ' ' } + " " + primerNombre.trim { it <= ' ' }
        if (nombreCompleto.length > 100) {
            nombreCompleto = nombreCompleto.substring(0, 100)
        }

        primerNombre = limpiaPalabra(primerNombre)
        primerNombre = CodPersona_QuitaNombres(primerNombre)
        primerApellido = limpiaPalabra(primerApellido)
        segundoApellido = limpiaPalabra(segundoApellido)

        var str1 = ""
        var str2 = ""
        var str3 = ""
        var str4 = ""
        var clave = ""

        var recFecha = ""
        var homonimo = ""
        var digito = ""

        if (primerApellido.trim { it <= ' ' }.length <= 2) {
            str4 = obtenLetra(primerApellido, 0)
            str1 = obtenLetra(segundoApellido, 0)
            str2 = obtenLetra(primerNombre, 0)
            str3 = obtenLetra(primerNombre, 1)
            clave = str4.trim { it <= ' ' } + str1.trim { it <= ' ' } + str2.trim { it <= ' ' } + str3.trim { it <= ' ' }
        } else {
            if (segundoApellido.trim { it <= ' ' }.length == 0) {
                str4 = obtenLetra(primerApellido, 0)
                str1 = obtenLetra(primerApellido, 0)
                str2 = obtenLetra(primerNombre, 0)
                str3 = obtenLetra(primerNombre, 1)
                clave = str4.trim { it <= ' ' } + str1.trim { it <= ' ' } + str2.trim { it <= ' ' } + str3.trim { it <= ' ' }
            } else {
                str4 = obtenLetra(primerApellido, 0)
                str1 = CodPersona_ObtenVocal(primerApellido)
                str2 = obtenLetra(primerNombre, 0)
                str3 = obtenLetra(segundoApellido, 0)
                clave = str4.trim { it <= ' ' } + str1.trim { it <= ' ' } + str3.trim { it <= ' ' } + str2.trim { it <= ' ' }
            }
        }

        clave = CodPersona_Regla8(clave)
        recFecha = obtenFecha(fechaNac)
        homonimo = obtenDifHomonimo(nombreCompleto)
        regresaCodPersona = clave + recFecha + homonimo
        digito = obtenDigVer(regresaCodPersona)

        regresaCodPersona = clave + recFecha + homonimo + digito
        regresaCodPersona = regresaCodPersona.toUpperCase()
        return regresaCodPersona
    }

    fun obtenDigVer(palabra: String): String {
        var palabra = palabra
        palabra = palabra.toUpperCase()
        var acumulado = 0
        var px = 0
        var tmpPalabra = ""
        var letra = ""
        /*set*/
        acumulado = 0
        px = 0
        tmpPalabra = palabra.trim { it <= ' ' }
        var digito = " "
        while (px <= tmpPalabra.trim { it <= ' ' }.length - 1) {
            letra = tmpPalabra[px].toString()
            when (letra) {
                " " -> acumulado += 37 * (14 - (px + 1))
                "Ñ", "ñ" -> acumulado += 24 * (14 - (px + 1))
                "A" -> acumulado += 10 * (14 - (px + 1))
                "B" -> acumulado += 11 * (14 - (px + 1))
                "C" -> acumulado += 12 * (14 - (px + 1))
                "D" -> acumulado += 13 * (14 - (px + 1))
                "E" -> acumulado += 14 * (14 - (px + 1))
                "F" -> acumulado += 15 * (14 - (px + 1))
                "G" -> acumulado += 16 * (14 - (px + 1))
                "H" -> acumulado += 17 * (14 - (px + 1))
                "I" -> acumulado += 18 * (14 - (px + 1))
                "J" -> acumulado += 19 * (14 - (px + 1))
                "K" -> acumulado += 20 * (14 - (px + 1))
                "L" -> acumulado += 21 * (14 - (px + 1))
                "M" -> acumulado += 22 * (14 - (px + 1))
                "N" -> acumulado += 23 * (14 - (px + 1))
                "O" -> acumulado += 25 * (14 - (px + 1))
                "P" -> acumulado += 26 * (14 - (px + 1))
                "Q" -> acumulado += 27 * (14 - (px + 1))
                "R" -> acumulado += 28 * (14 - (px + 1))
                "S" -> acumulado += 29 * (14 - (px + 1))
                "T" -> acumulado += 30 * (14 - (px + 1))
                "U" -> acumulado += 31 * (14 - (px + 1))
                "V" -> acumulado += 32 * (14 - (px + 1))
                "W" -> acumulado += 33 * (14 - (px + 1))
                "X" -> acumulado += 34 * (14 - (px + 1))
                "Y" -> acumulado += 35 * (14 - (px + 1))
                "Z" -> acumulado += 36 * (14 - (px + 1))
                "0" -> acumulado += 0 * (14 - (px + 1))
                "1" -> acumulado += 1 * (14 - (px + 1))
                "2" -> acumulado += 2 * (14 - (px + 1))
                "3" -> acumulado += 3 * (14 - (px + 1))
                "4" -> acumulado += 4 * (14 - (px + 1))
                "5" -> acumulado += 5 * (14 - (px + 1))
                "6" -> acumulado += 6 * (14 - (px + 1))
                "7" -> acumulado += 7 * (14 - (px + 1))
                "8" -> acumulado += 8 * (14 - (px + 1))
                "9" -> acumulado += 9 * (14 - (px + 1))
            }
            px++
        }

        px = acumulado % 11
        if (px == 0) {
            digito = "0"
        } else {
            px = 11 - px
            digito = px.toString().substring(0, 1)
        }
        return digito
    }

    fun limpiaPalabra(recibeString: String): String {
        var recibeString = recibeString
        recibeString = recibeString.toUpperCase()
        var regresaString = recibeString
        var p2 = 0
        var esp = 0
        var cicla = 0
        var vuelta = 0
        var espacio = 0
        var busca1 = ""
        var busca2 = ""

        vuelta = 0
        while (vuelta >= 0) {
            vuelta++
            if (vuelta == 11) {
                break
            }
            p2 = 1
            esp = 0
            cicla = 0
            if (vuelta == 1) {
                busca1 = " DE "
                busca2 = "DE "
                espacio = 3
            }

            if (vuelta == 2) {
                busca1 = " LA "
                busca2 = "LA "
                espacio = 3
            }
            if (vuelta == 3) {
                busca1 = " LAS "
                busca2 = "LAS "
                espacio = 4
            }
            if (vuelta == 4) {
                busca1 = " MC "
                busca2 = "MC "
                espacio = 3
            }
            if (vuelta == 5) {
                busca1 = " VON "
                busca2 = "VON "
                espacio = 4
            }
            if (vuelta == 6) {
                busca1 = " DEL "
                busca2 = "DEL "
                espacio = 4
            }
            if (vuelta == 7) {
                busca1 = " LOS "
                busca2 = "LOS "
                espacio = 4
            }
            if (vuelta == 8) {
                busca1 = " Y "
                busca2 = "Y "
                espacio = 2
            }
            if (vuelta == 9) {
                busca1 = " MAC "
                busca2 = "MAC "
                espacio = 4
            }
            if (vuelta == 10) {
                busca1 = " VAN "
                busca2 = "VAN "
                espacio = 4
            }

            while (cicla == 0) {
                p2 = regresaString.indexOf(busca1)//devuelve uno menos que en SQL
                if (p2 != -1) {
                    esp = espacio
                    regresaString = regresaString.substring(0, p2 + 1) + regresaString.substring(p2 + esp)
                } else {
                    p2 = regresaString.indexOf(busca2)
                    if (p2 == 0) {
                        esp = espacio
                        regresaString = regresaString.substring(p2 + esp)
                    } else {
                        break
                    }
                }
            }
        }
        return regresaString
    }

    fun obtenDifHomonimo(nombre: String): String {
        var suma = 0
        var cociente = 0
        var residuo = 0
        var cocienteStr = ""
        var residuoStr = ""
        var homonimo = ""
        homonimo = CodPersona_ObtenerValorUno(nombre)
        suma = CodPersona_ObtenerSuma(homonimo)
        suma = suma % 1000
        cociente = suma / 34
        residuo = suma % 34
        cocienteStr = CodPersona_ObtenValorDos(cociente)
        residuoStr = CodPersona_ObtenValorDos(residuo)
        homonimo = cocienteStr + residuoStr
        return homonimo
    }

    private fun CodPersona_ObtenValorDos(valor: Int): String {
        var valorStr = "0"
        when (valor) {
            0 -> valorStr = "1"
            1 -> valorStr = "2"
            2 -> valorStr = "3"
            3 -> valorStr = "4"
            4 -> valorStr = "5"
            5 -> valorStr = "6"
            6 -> valorStr = "7"
            7 -> valorStr = "8"
            8 -> valorStr = "9"
            9 -> valorStr = "A"
            10 -> valorStr = "B"
            11 -> valorStr = "C"
            12 -> valorStr = "D"
            13 -> valorStr = "E"
            14 -> valorStr = "F"
            15 -> valorStr = "G"
            16 -> valorStr = "H"
            17 -> valorStr = "I"
            18 -> valorStr = "J"
            19 -> valorStr = "K"
            20 -> valorStr = "L"
            21 -> valorStr = "M"
            22 -> valorStr = "N"
            23 -> valorStr = "P"
            24 -> valorStr = "Q"
            25 -> valorStr = "R"
            26 -> valorStr = "S"
            27 -> valorStr = "T"
            28 -> valorStr = "U"
            29 -> valorStr = "V"
            30 -> valorStr = "W"
            31 -> valorStr = "X"
            32 -> valorStr = "Y"
            33 -> valorStr = "Z"
        }
        return valorStr
    }

    private fun CodPersona_ObtenerSuma(valores: String): Int {
        var valor1: Int
        var valor2: Int
        var px = 0
        var suma = 0
        while (px < valores.trim { it <= ' ' }.length - 1) {
            valor1 = Integer.parseInt(valores.substring(px, px + 2))
            valor2 = Integer.parseInt(valores.substring(px + 1, px + 2))
            suma += valor1 * valor2
            px++
        }
        return suma
    }

    private fun CodPersona_ObtenerValorUno(nombre: String): String {
        var nombre = nombre
        nombre = nombre.toUpperCase()
        var valores = "0"
        var px = 1
        var letra: String
        while (px <= nombre.trim { it <= ' ' }.length) {
            letra = nombre.substring(px - 1, px)
            when (letra) {
                " " -> valores += "00"
                "ñ", "Ñ" -> valores += "10"
                "A" -> valores += "11"
                "B" -> valores += "12"
                "C" -> valores += "13"
                "D" -> valores += "14"
                "E" -> valores += "15"
                "F" -> valores += "16"
                "G" -> valores += "17"
                "H" -> valores += "18"
                "I" -> valores += "19"
                "J" -> valores += "21"
                "K" -> valores += "22"
                "L" -> valores += "23"
                "M" -> valores += "24"
                "N" -> valores += "25"
                "O" -> valores += "26"
                "P" -> valores += "27"
                "Q" -> valores += "28"
                "R" -> valores += "29"
                "S" -> valores += "32"
                "T" -> valores += "33"
                "U" -> valores += "34"
                "V" -> valores += "35"
                "W" -> valores += "36"
                "X" -> valores += "37"
                "Y" -> valores += "38"
                "Z" -> valores += "39"
            }
            px++
        }
        return valores
    }

    fun CodPersona_ObtenVocal(palabra: String): String {
        var palabra = palabra
        palabra = palabra.toUpperCase()
        var px = 1
        var vocal = ""
        try {
            var tmpPalabra = palabra
            vocal = tmpPalabra.substring(1, 2)
            if (tmpPalabra.trim { it <= ' ' }.length >= 2) {
                while (px <= tmpPalabra.trim { it <= ' ' }.length - 1) {
                    tmpPalabra = palabra.substring(px)
                    if (tmpPalabra.indexOf("A") == 0) {
                        try {
                            vocal = tmpPalabra.substring(0, 1).trim { it <= ' ' }
                        } catch (e: Exception) {
                            vocal = ""
                        }

                        break
                    }
                    if (tmpPalabra.indexOf("E") == 0) {
                        try {
                            vocal = tmpPalabra.substring(0, 1).trim { it <= ' ' }
                        } catch (e: Exception) {
                            vocal = ""
                        }

                        break
                    }
                    if (tmpPalabra.indexOf("I") == 0) {
                        try {
                            vocal = tmpPalabra.substring(0, 1).trim { it <= ' ' }
                        } catch (e: Exception) {
                            vocal = ""
                        }

                        break
                    }
                    if (tmpPalabra.indexOf("O") == 0) {
                        try {
                            vocal = tmpPalabra.substring(0, 1).trim { it <= ' ' }
                        } catch (e: Exception) {
                            vocal = ""
                        }

                        break
                    }
                    if (tmpPalabra.indexOf("U") == 0) {
                        try {
                            vocal = tmpPalabra.substring(0, 1).trim { it <= ' ' }
                        } catch (e: Exception) {
                            vocal = ""
                        }

                        break
                    }
                    px++
                }
            }
        } catch (e: Exception) {
            Log.e("CodPersona_ObtenVocal", e.message)
        }

        return vocal
    }

    private fun CodPersona_QuitaNombres(recibeString: String): String {
        var recibeString = recibeString
        recibeString = recibeString.toUpperCase()
        var regresaString = recibeString
        var p1 = 0
        p1 = regresaString.indexOf("MARIA JOSE")
        if (p1 == 0) {
            return regresaString
        }
        p1 = regresaString.indexOf("JOSE MARIA")
        if (p1 == 0) {
            return regresaString
        }
        var Str1: String
        p1 = regresaString.indexOf("MARIA ")
        if (p1 == 0) {
            Str1 = ""
            try {
                Str1 = regresaString.substring(6).trim { it <= ' ' }
            } catch (e: Exception) {
                Log.e("message", e.message)
            }

            if (Str1 != "") {
                regresaString = Str1
            }
        }
        p1 = regresaString.indexOf("JOSE ")
        if (p1 == 0) {
            Str1 = ""
            try {
                Str1 = regresaString.substring(5).trim { it <= ' ' }
            } catch (e: Exception) {
                Log.e("message", e.message)
            }

            if (Str1 != "") {
                regresaString = Str1
            }
        }
        return regresaString
    }

    fun CodPersona_QuitaEspacios(recibeString: String): String {
        var regresaString = recibeString
        val indice = recibeString.trim { it <= ' ' }.indexOf(' ')
        while (indice < regresaString.trim { it <= ' ' }.length - 1) {
            if (indice == -1) {
                return regresaString
            }
            if (indice == 0) {
                regresaString = regresaString.substring(indice + 1)
            } else {
                regresaString = regresaString.substring(0, indice) + regresaString.substring(indice + 1)
            }
        }
        return regresaString
    }

    fun fechaJuliana(date: Date): Int {
        var days = 0
        try {
            val start = DateTime(DateUtils.toShortFormatSlash("28/12/1800"))
            days = Days.daysBetween(start, DateTime(date)).getDays()
        } catch (e: Exception) {
            Log.e("fechaJuliana", e.message)
        }

        return days
    }

    fun obtenFecha(fecha: String): String {
        var regresar = ""
        try {
            regresar = DateUtils.convertToUniversalCode(fecha)
        } catch (e: ParseException) {
            Log.e("obtenFecha", e.message)
        }

        return regresar
    }

    fun obtenLetra(palabra: String, posicion: Int): String {
        var regresaLetra = ""
        try {
            regresaLetra = palabra.substring(posicion, posicion + 1)
        } catch (e: Exception) {
            Log.e("obtenLetra", e.message)
        }

        return regresaLetra
    }

    private fun CodPersona_Regla8(palabra: String): String {
        var palabra = palabra
        palabra = palabra.toUpperCase()
        var regresa = palabra
        var busca = ""
        var cicla = 0
        while (cicla >= 0) {
            cicla++
            when (cicla) {
                1 -> busca = "BUEI"
                2 -> busca = "BUEY"
                3 -> busca = "CACA"
                4 -> busca = "CACO"
                5 -> busca = "CAKA"
                6 -> busca = "CAKO"
                7 -> busca = "LOCA"
                8 -> busca = "LOCO"
                9 -> busca = "LOKA"
                10 -> busca = "LOKO"
                11 -> busca = "COGE"
                12 -> busca = "KOGE"
                13 -> busca = "KOJO"
                14 -> busca = "KAKA"
                15 -> busca = "KULO"
                16 -> busca = "MAME"
                17 -> busca = "MAMO"
                18 -> busca = "MEAR"
                19 -> busca = "MEON"
                20 -> busca = "MION"
                21 -> busca = "COJE"
                22 -> busca = "CULO"
                23 -> busca = "FETO"
                24 -> busca = "GUEY"
                25 -> busca = "JOTO"
                26 -> busca = "KACA"
                27 -> busca = "KAGA"
                28 -> busca = "MOCO"
                29 -> busca = "MULA"
                30 -> busca = "PEDA"
                31 -> busca = "PENE"
                32 -> busca = "PUTA"
                33 -> busca = "QULO"
                34 -> busca = "RATA"
                35 -> busca = "RUIN"
                36 -> busca = "KACO"
                37 -> busca = "KAGO"
                38 -> busca = "PEDO"
                39 -> busca = "PUTO"
                40 -> busca = "COJI"
                41 -> busca = "COJO"
                42 -> busca = "COJA"
                43 -> busca = "MEAS"
                44 -> {
                }
            }
            if (cicla == 44) {
                break
            } else {
                if (palabra.indexOf(busca) == 0) {
                    regresa = busca.substring(0, 3) + "X"
                }
            }
        }
        return regresa
    }
}