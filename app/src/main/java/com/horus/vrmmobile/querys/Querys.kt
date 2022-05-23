package com.horus.vrmmobile.querys

import com.horus.vrmmobile.Utils.DateUtils
import java.util.*

object Querys {
    fun getItemSincronizacion(): String {
        return "SELECT temp.CodFormulario,v3.Version,v3.CodFormulario,v3.CodVersion, \n" +
                "      Sum(temp.Encuestas) as Encuestas,\n" +
                "      Sum(temp.Imposibilitada) as Imposibilitadas,\n" +
                "      Sum(temp.PuntoInteres) as PuntoInteres,\n" +
                "      Sum(temp.TotalMultimedia) AS TotalMultimedia \n" +
                "FROM (\n" +
                "   SELECT \n" +
                "       v.CodFormulario,\n" +
                "       sum(case when (e.Estado in('COD_COMPLETA','COD_FINALIZADA_INCOMPLETA','COD_EN_PROCESO','COD_DE_PRUEBA') ) then 1 else 0 end) as Encuestas,\n" +
                "       sum(case when e.Estado ='COD_IMPOSIBILITADA' then 1 else 0 end) as Imposibilitada,\n" +
                "       sum(case when e.Estado ='COD_PUNTO_INTERES' then 1 else 0 end) as PuntoInteres,\n" +
                "       0 AS TotalMultimedia \n" +
                "   FROM encuestas AS e\n" +
                "   INNER JOIN versiones as v ON e.CodVersion = v.CodVersion\n" +
                "   WHERE e.Estado <> '' AND e.Activo = 1 AND e.sincronizado = 0 \n" +
                "   GROUP BY v.CodFormulario\n" +
                "   UNION\n" +
                "   SELECT \n" +
                "       v.CodFormulario,0,0,0,\n" +
                "       sum(case when (m.CodTipoMultimedia in('COD_VIDEO','COD_IMAGEN','COD_AUDIO') ) then 1 else 0 end) as TotalMultimedia\n" +
                "   FROM encuestas AS e\n" +
                "   INNER JOIN versiones AS v ON e.CodVersion = v.CodVersion \n" +
                "   INNER JOIN Multimedia AS m ON m.CodVisita = e.CodEncuesta \n" +
                "   WHERE e.Estado <> '' AND e.Activo = 1 AND m.sincronizado = 0 AND m.Activo = 1 \n" +
                "   GROUP BY v.CodFormulario\n" +
                ") AS temp \n" +
                "INNER JOIN versiones AS v3 ON v3.CodFormulario = temp.CodFormulario \n" +
                "WHERE temp.CodFormulario is not null \n" +
                "GROUP BY temp.CodFormulario;"
    }
    fun insertSyncLog(uuid: String, codFormulario: String, codMedioSync: String): String {
        return "insert into SyncLog (DeviceCode,Fecha,Sync,CodFormulario,CodMedioSincronizacion) " +
                "values ('" + uuid + "', strftime('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'), 0,'" + codFormulario + "','" + codMedioSync + "');"
    }

    fun selectDetalleEncuestasSincronizar(codVersion: String): String {
        return "SELECT t.CodFormulario,\n" +
                "       t.Fecha, \n" +
                "       sum(t.TotalEncuestas) AS TotalEncuestas,\n" +
                "       sum(t.Encuestas) AS Encuestas,\n" +
                "       sum(t.Completa) AS Completa,\n" +
                "       sum(t.FinalizadaIncompleta) AS FinalizadaIncompleta,\n" +
                "       sum(t.EnProceso) AS EnProceso,\n" +
                "       sum(t.Revisita) AS Revisita,\n" +
                "       sum(t.Imposibilitada) AS Imposibilitada,\n" +
                "       sum(t.PuntoInteres) AS PuntoInteres,\n" +
                "       sum(t.DePrueba) AS DePrueba,\n" +
                "       sum(t.DePrueba) AS Vacia,\n" +
                "       sum(t.TotalMultimedia) AS TotalMultimedia,\n" +
                "       sum(t.Imagen) AS Imagen,\n" +
                "       sum(t.Video) AS Video,\n" +
                "       sum(t.Audio) AS Audio\n" +
                "FROM (\n" +
                "SELECT \n" +
                "    v.CodFormulario,strftime('%d-%m-%Y', e.FechaEncuesta) Fecha,\n" +
                "    sum(case when (e.Estado in('COD_COMPLETA','COD_FINALIZADA_INCOMPLETA','COD_EN_PROCESO','COD_REVISITA','COD_IMPOSIBILITADA','COD_DE_PRUEBA','COD_PUNTO_INTERES') ) then 1 else 0 end) as TotalEncuestas, \n" +
                "    sum(case when (e.Estado in('COD_COMPLETA','COD_FINALIZADA_INCOMPLETA','COD_EN_PROCESO','COD_DE_PRUEBA') ) then 1 else 0 end) as Encuestas,\n" +
                "    sum(case when e.Estado ='COD_COMPLETA' then 1 else 0 end) as Completa,\n" +
                "    sum(case when e.Estado ='COD_FINALIZADA_INCOMPLETA' then 1 else 0 end) as FinalizadaIncompleta,\n" +
                "    sum(case when e.Estado ='COD_EN_PROCESO' then 1 else 0 end) as EnProceso,\n" +
                "    sum(case when e.Estado ='COD_REVISITA' then 1 else 0 end) as Revisita,\n" +
                "    sum(case when e.Estado ='COD_IMPOSIBILITADA' then 1 else 0 end) as Imposibilitada,\n" +
                "    sum(case when e.Estado ='COD_PUNTO_INTERES' then 1 else 0 end) as PuntoInteres,\n" +
                "    sum(case when e.Estado ='COD_DE_PRUEBA' then 1 else 0 end) as DePrueba, \n" +
                "    sum(case when trim(e.Estado) = '' then 1 else 0 end) as Vacia,\n" +
                "    0 AS TotalMultimedia,\n" +
                "    0 AS Imagen,\n" +
                "    0 AS Video,\n" +
                "    0 AS Audio\n" +
                "FROM encuestas AS e\n" +
                "INNER JOIN versiones AS v ON e.CodVersion = v.CodVersion\n" +
                "WHERE e.Activo = 1 AND e.sincronizado = 0 AND e.CodVersion = '" + codVersion + "'\n" +
                "GROUP BY v.CodFormulario, strftime('%d-%m-%Y', e.FechaEncuesta)\n" +
                "UNION\n" +
                "SELECT \n" +
                "    v.CodFormulario, strftime('%d-%m-%Y', e.FechaEncuesta) Fecha,0,0,0,0,0,0,0,0,0,0,\n" +
                "    sum(case when (m.CodTipoMultimedia in('COD_VIDEO','COD_IMAGEN','COD_AUDIO') ) then 1 else 0 end) as TotalMultimedia,\n" +
                "    sum(case when m.CodTipoMultimedia ='COD_IMAGEN' then 1 else 0 end) as Imagen,\n" +
                "    sum(case when m.CodTipoMultimedia ='COD_VIDEO' then 1 else 0 end) as Video, \n" +
                "    sum(case when m.CodTipoMultimedia ='COD_AUDIO' then 1 else 0 end) as Audio    \n" +
                "FROM encuestas AS e\n" +
                "INNER JOIN versiones AS v ON e.CodVersion = v.CodVersion\n" +
                "INNER JOIN Multimedia AS m ON m.CodVisita = e.CodEncuesta\n" +
                "WHERE e.Activo = 1 AND m.sincronizado = 0 AND m.Activo = 1 AND e.CodVersion = '" + codVersion + "'\n" +
                "GROUP BY v.CodFormulario, strftime('%d-%m-%Y', e.FechaEncuesta)\n" +
                ") AS t \n" +
                "GROUP BY t.CodFormulario,t.Fecha"
    }

    fun getClavesByBuscadorCenso(codversion: String, buscar: String): String {
        return "SELECT ps.CodPreguntaSeccion,ps.TextoAlternativo,td.CodTipoDatoGeneral,\n" +
                "CASE WHEN td.CodTipoDatoGeneral='ALFNUM' THEN 'r.CodPreguntaSeccion = '''||ps.CodPreguntaSeccion||''' \n" +
                "AND r.RespuestaAlfanumerico LIKE ''%" + buscar + "%'' '\n" +
                "ELSE 'r.CodPreguntaSeccion = '''||ps.CodPreguntaSeccion||''' AND r.CodOpcion LIKE ''%" + buscar + "%'' '\n" +
                "END as Condicion\n" +
                "FROM preguntas_secciones as ps\n" +
                "INNER JOIN secciones_modulos as sm ON sm.CodSeccionModulo = ps.CodSeccionModulo\n" +
                "INNER JOIN modulos as m ON m.CodModulo = sm.CodModulo\n" +
                "INNER JOIN tipo_datos as td ON td.CodTipoDato = ps.CodTipoDato\n" +
                "WHERE ps.ClaveBusqueda = 1\n" +
                "AND m.CodVersion = '" + codversion + "'\n" +
                "AND ps.Activo = 1"
    }

    fun getOpcionesTerreno(codVersion: String): String {
        return "select  ifnull(group_concat('''' || op.codopcion || '''',','),'') \n" +
                "                from opciones_preguntas as op\n" +
                "                INNER JOIN secciones_modulos smm on smm.CodSeccionModulo = op.CodSeccionModulo\n" +
                "                INNER JOIN modulos mm on mm.CodModulo = smm.CodModulo and mm.codVersion = '" + codVersion + "'\n" +
                "                where op.codpreguntaseccion='COD_PREG_SEC_42157_RELEVAMIENTO' and op.activo=1 \n" +
                "                and op.codopcion not in ('COD_CASA_ACEPTA_LA_ENCUESTA','COD_SITUACION_DE_CALLE','COD_VIVIENDA_MOVIL_O_IMPROVISADA')\n" +
                "                order by op.orden asc"
    }

    fun getEncuestasByBuscadorCenso(codversion: String, conditions: String, fromDate: Date, toDate: Date, buscar: String, opcionesTerreno: String): String {
        val fromString = DateUtils.convertDateShortNotUTCToString(fromDate)
        val toString = DateUtils.convertDateShortNotUTCToString(toDate)
        var fechas = ""
        if (fromString.isNotEmpty()) {
            fechas += " AND date(e.FechaEncuesta) >= '$fromString' "
        }
        if (toString.isNotEmpty()) {
            fechas += " AND date(e.FechaEncuesta) <= '$toString' "
        }
        return "SELECT distinct e.CodEncuesta,e.NombreEncuesta, CASE WHEN t.TextoOpcionEncuestaRechazo<>'' THEN t.TextoOpcionEncuestaRechazo \n" +
                "WHEN t2.JefeDeHogar<>'' THEN t2.JefeDeHogar ELSE '' END AS resultado,\n" +
                " CASE WHEN e.Estado IS NULL OR e.Estado = '' THEN 'COD_EN_PROCESO' ELSE e.Estado END AS Estado,\n" +
                " CASE WHEN ee.estadoencuesta IS NULL OR ee.estadoencuesta = '' THEN 'En proceso' ELSE ee.estadoencuesta END AS EstadoEncuesta,\n" +
                " e.Sincronizado, e.FechaEncuesta, e.FechaFinal AS FechaModificacion,e.CodUsuarioRevisita, \n" +
                " v.CodVivienda,strftime('%Y-%m-%d',ifnull(e.FechaFinal,'now')) as FechaFinal,ifnull(ub.Nim,0) NIM \n" +
                "FROM encuestas as e\n" +
                "INNER JOIN viviendas as v ON v.CodEncuesta = e.CodEncuesta \n" +
                "LEFT JOIN EstadosEncuesta as ee ON ee.codestadoencuesta = e.Estado \n" +
                "LEFT JOIN respuestas as r ON r.CodEncuesta = e.CodEncuesta\n" +
                "LEFT JOIN(\n" +
                "SELECT DISTINCT r.CodEncuesta,\n" +
                "CASE WHEN (r.CodOpcion IN(" + opcionesTerreno + ") ) THEN op.TextoAlternativo ELSE '' END as TextoOpcionEncuestaRechazo\n" +
                "FROM opciones_preguntas as op\n" +
                "LEFT JOIN respuestas as r ON r.CodOpcion = op.CodOpcion AND r.CodPreguntaSeccion = op.CodPreguntaSeccion\n" +
                "WHERE r.CodVersion='" + codversion + "' \n" +
                "AND r.CodPreguntaSeccion='COD_PREG_SEC_42157_RELEVAMIENTO'\n" +
                ") as t ON t.CodEncuesta = e.CodEncuesta\n" +
                "LEFT JOIN(\n" +
                "SELECT fv.CodVisita as CodEncuesta,p.Apellidos||', '||p.Nombres as JefeDeHogar\n" +
                "FROM Personas as p\n" +
                "INNER JOIN Personas_Familias AS pf ON pf.CodPersona = p.CodPersona\n" +
                "INNER JOIN Familias_Visitas as fv ON fv.CodFamiliaVisita = pf.CodFamiliaVisita\n" +
                "INNER JOIN encuestas as e ON e.CodEncuesta = fv.CodVisita\n" +
                "WHERE e.CodVersion = '" + codversion + "' AND pf.JefeDeHogar = 1 \n" +
                ") as t2 ON t2.CodEncuesta = e.CodEncuesta\n" +
                "LEFT JOIN(\n" +
                "SELECT r.CodEncuesta,RespuestaAlfanumerico as nim\n" +
                "FROM respuestas as r \n" +
                "WHERE r.CodVersion='" + codversion + "' \n" +
                "AND r.CodPreguntaSeccion='COD_PREG_SEC_476_RELEVAMIENTO'\n" +
                ") as ub ON ub.CodEncuesta = e.CodEncuesta \n" +
                "WHERE e.CodVersion='" + codversion + "'\n" +
                "AND (( e.NombreEncuesta LIKE '%" + buscar + "%') \n" +
                conditions + " ) " + fechas + " AND e.Activo = 1 "
    }

    fun getEncuestasRevisitas(cod_version: String): String {
        return " AND e.CodEncuesta IN (" +
                "SELECT e.CodEncuesta " +
                "FROM encuestas AS e " +
                "WHERE e.codversion='" + cod_version + "' AND e.CodUsuarioRevisita != '')"
    }

    fun getClavesByBuscadorCensoNuevaBusqueda(codversion: String, buscar: String): String {
        return "SELECT ps.CodPreguntaSeccion,ps.TextoAlternativo,td.CodTipoDatoGeneral,ps.CodPreguntaSeccion as Condicion\n" +
                "FROM preguntas_secciones as ps\n" +
                "INNER JOIN secciones_modulos as sm ON sm.CodSeccionModulo = ps.CodSeccionModulo\n" +
                "INNER JOIN modulos as m ON m.CodModulo = sm.CodModulo\n" +
                "INNER JOIN tipo_datos as td ON td.CodTipoDato = ps.CodTipoDato\n" +
                "WHERE ps.ClaveBusqueda = 1\n" +
                "AND m.CodVersion = '" + codversion + "'\n" +
                "AND ps.Activo = 1;"
    }

    fun getEncuestasReferenciaCloneNew(cod_version: String, condicion: String, condicion_palabras: String): String {
        return "SELECT DISTINCT p.codEncuesta AS CodEncuesta,p.nombreEncuesta as NombreEncuesta,cadena as resultado, '', p.EstadoEncuesta, p.CodVivienda,strftime('%Y-%m-%d %H:%M',ifnull(p.FechaFinal,'now')) as FechaFinal, p.FechaEncuesta, ifnull(n.RespuestaAlfanumerico,'-') as NIM \n" +
                "FROM( \n" +
                " SELECT group_concat(t.total,x'0a') as cadena,t.codEncuesta,t.NombreEncuesta, t.EstadoEncuesta,t.CodVivienda,t.FechaFinal,t.FechaEncuesta \n" +
                " FROM ( \n" +
                " SELECT r2.codEncuesta,r2.codEntidad,group_concat(case when r2.CodPreguntaSeccion = 'COD_PREG_SEC_476_RELEVAMIENTO' then 'Nim: ' || r2.respuestaalfanumerico else r2.respuestaalfanumerico end,' - ') as total,e.NombreEncuesta, ifnull(ee.EstadoEncuesta,'En Proceso') AS EstadoEncuesta, v.CodVivienda as CodVivienda,e.FechaFinal as FechaFinal, e.FechaEncuesta \n" +
                " FROM respuestas r2 \n" +
                " INNER JOIN encuestas e on e.codEncuesta = r2.codEncuesta AND e.CodVersionesDestino LIKE '%" + cod_version + "%' \n" +
                " INNER JOIN viviendas AS v ON e.CodEncuesta = v.CodEncuesta AND v.CodVersionesDestino LIKE '%" + cod_version + "%' \n" +
                " LEFT JOIN EstadosEncuesta ee on ee.codestadoencuesta = e.estado \n" +
                " WHERE e.PermiteCopia = 1 \n" +
                " AND e.CodUsuarioRevisita = '' \n" +
                " AND r2.CodVersionesDestino LIKE '%" + cod_version + "%' \n" +
                " AND r2.codPreguntaSeccion IN (" + condicion + ") \n" +
                " GROUP BY r2.CodEncuesta, r2.codEntidad\n" +
                " ORDER BY r2.idRespuesta\n" +
                " ) t\n" +
                " GROUP BY t.codEncuesta\n" +
                ") AS p \n" +
                "LEFT JOIN ( \n" +
                " SELECT r3.CodEncuesta,r3.RespuestaAlfanumerico \n" +
                " FROM respuestas AS r3 \n" +
                " WHERE r3.codVersion = '" + cod_version + "' \n" +
                " AND r3.codPreguntaSeccion IN ('COD_PREG_SEC_476_RELEVAMIENTO') \n" +
                ") as n ON p.CodEncuesta = n.CodEncuesta \n" +
                "WHERE " + condicion_palabras
    }

    fun getEncuestasReferencia(cod_version: String, condicion: String, condicion_palabras: String): String {
        return "SELECT DISTINCT p.codEncuesta AS CodEncuesta,p.nombreEncuesta as NombreEncuesta,cadena as resultado, '', p.EstadoEncuesta, p.CodVivienda,strftime('%Y-%m-%d %H:%M',ifnull(p.FechaFinal,'now')) as FechaFinal, p.FechaEncuesta, ifnull(n.RespuestaAlfanumerico,'-') as NIM \n" +
                "FROM( \n" +
                "    SELECT group_concat(t.total,x'0a') as cadena,t.codEncuesta,t.NombreEncuesta, t.EstadoEncuesta,t.CodVivienda,t.FechaFinal,t.FechaEncuesta \n" +
                "    FROM ( \n" +
                "        SELECT r2.codEncuesta,r2.codEntidad,group_concat(case when r2.CodPreguntaSeccion = 'COD_PREG_SEC_476_RELEVAMIENTO' then 'Nim: ' || r2.respuestaalfanumerico else r2.respuestaalfanumerico end,' - ') as total,e.NombreEncuesta, ifnull(ee.EstadoEncuesta,'En Proceso') AS EstadoEncuesta, v.CodVivienda as CodVivienda,e.FechaFinal as FechaFinal, e.FechaEncuesta \n" +
                "        FROM respuestas r2 \n" +
                "        INNER JOIN encuestas e on e.codEncuesta = r2.codEncuesta \n" +
                "        INNER JOIN viviendas AS v ON e.CodEncuesta = v.CodEncuesta \n" +
                "        LEFT JOIN EstadosEncuesta ee on ee.codestadoencuesta = e.estado \n" +
                "        WHERE e.PermiteCopia = 1 \n" +
                "        AND e.CodUsuarioRevisita = '' \n" +
                "        AND r2.codVersion = '" + cod_version + "'\n" +
                "        AND ee.CodEstadoEncuesta <> 'COD_EN_PROCESO' \n" +
                "        AND ee.CodEstadoEncuesta <> 'COD_PUNTO_INTERES' \n" +
                "        AND ee.CodEstadoEncuesta <> 'COD_IMPOSIBILITADA' \n" +
                "        AND r2.codPreguntaSeccion IN (" + condicion + ") \n" +
                //"        AND r2.CodPreguntaSeccion <> 'COD_PREG_SEC_476_RELEVAMIENTO' \n" +
                "        GROUP BY r2.CodEncuesta, r2.codEntidad\n" +
                "        ORDER BY r2.idRespuesta\n" +
                "    ) t\n" +
                "    GROUP BY t.codEncuesta\n" +
                ") AS p \n" +
                "LEFT JOIN ( \n" +
                "    SELECT r3.CodEncuesta,r3.RespuestaAlfanumerico \n" +
                "    FROM respuestas AS r3 \n" +
                "    WHERE r3.codVersion = '" + cod_version + "' \n" +
                "    AND r3.codPreguntaSeccion IN ('COD_PREG_SEC_476_RELEVAMIENTO') \n" +
                ") as n ON p.CodEncuesta = n.CodEncuesta \n" +
                "WHERE " + condicion_palabras
    }
}