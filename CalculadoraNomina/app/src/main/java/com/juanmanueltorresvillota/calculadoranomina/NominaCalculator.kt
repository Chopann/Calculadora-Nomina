package com.juanmanueltorresvillota.calculadoranomina

// Salario minimo en colombia
const val SMMLV = 1750905.0

// Auxilio de transporte
const val AUXILIO_TRANSPORTE = 249095.0

// Horas ordinarias
const val HORAS_ORDINARIAS = 210.0

// Porcentaje salud 4% (0.04)
const val PORCENTAJE_SALUD = 0.04

// Porcentaje pension 4% (0.04)
const val PORCENTAJE_PENSION = 0.04

// Fondo de solidaridad pensional 1% (0.01)
const val PORCENTAJE_FONDO_SOLIDARIO = 0.01

const val LIMITES_2_SMMLV = 2 * SMMLV
const val LIMITES_4_SMMLV = 4 * SMMLV

// Máximo de horas extra al mes: la ley permite 12 por semana; el taller toma 4 semanas
const val MAX_HORAS_EXTRA = 48.0


data class ResultadoNomina(
    val valorHora: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double
)


enum class RangoSalarial { RANGO_1, RANGO_2, RANGO_3 }

fun clasificarRango(salarioBasico: Double): RangoSalarial {
    return when {
        salarioBasico <= LIMITES_2_SMMLV -> RangoSalarial.RANGO_1
        salarioBasico < LIMITES_4_SMMLV -> RangoSalarial.RANGO_2
        else -> RangoSalarial.RANGO_3
    }
}

fun calcularNomina (
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {

    // Valor de la hora ordinaria
    val valorHora = salarioBasico / HORAS_ORDINARIAS

    // Valor de las horas extra
    val factorDiurno = if (esDominical) 2.15 else 1.25
    val factorNocturno = if (esDominical) 2.65 else 1.75
    val pagoExtrasDiurnas = horasDiurnas * valorHora * factorDiurno
    val pagoExtrasNocturnas = horasNocturnas * valorHora * factorNocturno
    val totalHorasExtra = pagoExtrasDiurnas + pagoExtrasNocturnas

    // Ingreso base cotizacion
    val ibc = salarioBasico + totalHorasExtra

    // Auxilio de transporte
    val auxilioTransporte = if (salarioBasico <= LIMITES_2_SMMLV && !transporteEmpresa) AUXILIO_TRANSPORTE else 0.0

    // Total devengado
    val totalDevengado = ibc + auxilioTransporte

    // Deducciones
    val aporteSalud = ibc * PORCENTAJE_SALUD
    val aportePension = ibc * PORCENTAJE_PENSION
    val fondoSolidaridad =
        if (ibc >= LIMITES_4_SMMLV) ibc * PORCENTAJE_FONDO_SOLIDARIO else 0.0
    val totalDeducciones = aporteSalud + aportePension + fondoSolidaridad

    val salarioNeto = totalDevengado - totalDeducciones

    return ResultadoNomina(
        valorHora = valorHora,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto
    )

}