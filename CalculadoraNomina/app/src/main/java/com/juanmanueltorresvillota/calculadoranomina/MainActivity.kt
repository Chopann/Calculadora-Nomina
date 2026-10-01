package com.juanmanueltorresvillota.calculadoranomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanmanueltorresvillota.calculadoranomina.ui.theme.CalculadoraNominaTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraNominaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaNomina(modifier = Modifier.padding(innerPadding))

                }
            }
        }
    }
}

@Composable
fun CampoNumerico(
    @StringRes etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(stringResource(etiqueta)) },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        isError = isError,
        modifier = modifier
    )
}

@Composable
fun PantallaNomina(modifier: Modifier = Modifier) {
    var salario by remember { mutableStateOf("") }
    var horasDiurnas by remember { mutableStateOf("") }
    var horasNocturnas by remember { mutableStateOf("") }
    var esDominical by remember { mutableStateOf(false) }
    var transporteEmpresa by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoNomina?>(null) }
    var rango by remember { mutableStateOf<RangoSalarial?>(null) }
    var salarioConError by remember { mutableStateOf(false) }
    var diurnasConError by remember { mutableStateOf(false) }
    var nocturnasConError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<Int?>(null) }

    fun mostrarError(
        @StringRes mensaje: Int,
        salarioMal: Boolean = false,
        diurnasMal: Boolean = false,
        nocturnasMal: Boolean = false
    ) {
        mensajeError = mensaje
        salarioConError = salarioMal
        diurnasConError = diurnasMal
        nocturnasConError = nocturnasMal
        resultado = null
        rango = null
    }

    fun calcular() {
        // 1. Borrar los errores de intentos anteriores
        salarioConError = false
        diurnasConError = false
        nocturnasConError = false
        mensajeError = null

        // 2. Convertir los textos (dan null si no son números válidos)
        val salarioNumero = salario.toDoubleOrNull()
        val diurnasNumero = convertirHoras(horasDiurnas)
        val nocturnasNumero = convertirHoras(horasNocturnas)

        // 3. Validar, de una en una
        if (salarioNumero == null) {
            mostrarError(R.string.error_salario_invalido, salarioMal = true)
            return
        }
        if (salarioNumero < SMMLV) {
            mostrarError(R.string.error_salario_minimo, salarioMal = true)
            return
        }
        if (diurnasNumero == null || diurnasNumero < 0) {
            mostrarError(R.string.error_horas_invalidas, diurnasMal = true)
            return
        }
        if (nocturnasNumero == null || nocturnasNumero < 0) {
            mostrarError(R.string.error_horas_invalidas, nocturnasMal = true)
            return
        }
        if (diurnasNumero + nocturnasNumero > MAX_HORAS_EXTRA) {
            mostrarError(R.string.error_horas_exceso, diurnasMal = true, nocturnasMal = true)
            return
        }

        // 4. Si llegamos aquí, todos los datos son válidos
        resultado = calcularNomina(
            salarioNumero, diurnasNumero, nocturnasNumero, esDominical, transporteEmpresa
        )
        rango = clasificarRango(salarioNumero)
    }

    fun limpiar() {
        salario = ""
        horasDiurnas = ""
        horasNocturnas = ""
        esDominical = false
        transporteEmpresa = false
        resultado = null
        rango = null
        salarioConError = false
        diurnasConError = false
        nocturnasConError = false
        mensajeError = null
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Juan Manuel Torres",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.titulo), style = MaterialTheme.typography.headlineSmall
        )

        CampoNumerico(
            etiqueta = R.string.salario_basico,
            valor = salario,
            onValueChange = { salario = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        CampoNumerico(
            etiqueta = R.string.horas_diurnas,
            valor = horasDiurnas,
            onValueChange = { horasDiurnas = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        CampoNumerico(
            etiqueta = R.string.horas_nocturnas,
            valor = horasNocturnas,
            onValueChange = { horasNocturnas = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        FilaInterruptor(
            etiqueta = R.string.domingo_festivo, valor = esDominical, onCheckedChange = {
                esDominical = it
                if (resultado != null) calcular()
            })

        FilaInterruptor(
            etiqueta = R.string.transporte_empresa, valor = transporteEmpresa, onCheckedChange = {
                transporteEmpresa = it
                if (resultado != null) calcular()
            })
        mensajeError?.let {
            Text(
                text = stringResource(it), color = MaterialTheme.colorScheme.error
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { calcular() }) {
                Text(stringResource(R.string.boton_calcular))
            }
            OutlinedButton(onClick = { limpiar() }) {
                Text(stringResource(R.string.boton_limpiar))
            }
        }

        rango?.let { InfoRango(rango = it) }

        resultado?.let { r ->
            HorizontalDivider()

            Text(
                text = stringResource(R.string.seccion_devengado),
                style = MaterialTheme.typography.titleMedium
            )
            FilaResultado(R.string.valor_hora, formatearMoneda(r.valorHora))
            FilaResultado(
                R.string.concepto_salario_basico, formatearMoneda(r.valorHora * HORAS_ORDINARIAS)
            )
            FilaResultado(R.string.horas_extra, formatearMoneda(r.totalHorasExtra))
            FilaResultado(R.string.auxilio_transporte, formatearMoneda(r.auxilioTransporte))
            FilaResultado(
                R.string.total_devengado, formatearMoneda(r.totalDevengado), negrita = true
            )

            HorizontalDivider()

            Text(
                text = stringResource(R.string.seccion_deducciones),
                style = MaterialTheme.typography.titleMedium
            )
            FilaResultado(R.string.aporte_salud, formatearMoneda(r.aporteSalud))
            FilaResultado(R.string.aporte_pension, formatearMoneda(r.aportePension))
            FilaResultado(R.string.fondo_solidaridad, formatearMoneda(r.fondoSolidaridad))
            FilaResultado(
                R.string.total_deducciones, formatearMoneda(r.totalDeducciones), negrita = true
            )

            HorizontalDivider()

            FilaResultado(
                R.string.salario_neto,
                formatearMoneda(r.salarioNeto),
                negrita = true,
                estilo = MaterialTheme.typography.titleLarge
            )
        }
    }
}

fun formatearMoneda(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formato.maximumFractionDigits = 0
    return formato.format(valor)
}

fun convertirHoras(texto: String): Double? {
    return if (texto.isBlank()) 0.0 else texto.toDoubleOrNull()
}

@Composable
fun FilaInterruptor(
    @StringRes etiqueta: Int,
    valor: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(etiqueta), modifier = Modifier.weight(1f)
        )
        Switch(
            checked = valor, onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FilaResultado(
    @StringRes etiqueta: Int,
    valor: String,
    modifier: Modifier = Modifier,
    negrita: Boolean = false,
    estilo: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val peso = if (negrita) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(etiqueta),
            style = estilo,
            fontWeight = peso,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor, style = estilo, fontWeight = peso
        )
    }
}

@Composable
fun InfoRango(rango: RangoSalarial, modifier: Modifier = Modifier) {
    val (imagen, descripcion, texto) = when (rango) {
        RangoSalarial.RANGO_1 -> Triple(
            R.drawable.ic_rango_1, R.string.rango_1_descripcion, R.string.rango_1_texto
        )

        RangoSalarial.RANGO_2 -> Triple(
            R.drawable.ic_rango_2, R.string.rango_2_descripcion, R.string.rango_2_texto
        )

        RangoSalarial.RANGO_3 -> Triple(
            R.drawable.ic_rango_3, R.string.rango_3_descripcion, R.string.rango_3_texto
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(imagen),
            contentDescription = stringResource(descripcion),
            modifier = Modifier.size(96.dp)
        )
        Text(
            text = stringResource(texto), textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaNominaPreview() {
    CalculadoraNominaTheme {
        PantallaNomina()
    }
}


