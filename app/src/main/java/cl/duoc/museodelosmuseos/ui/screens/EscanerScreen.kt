package cl.duoc.museodelosmuseos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.viewmodel.EscanerViewModel
import cl.duoc.museodelosmuseos.viewmodel.ResultadoEscaneo
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Analiza cada cuadro de la cámara y avisa cuando encuentra un QR
@ExperimentalGetImage
private class QrAnalyzer(private val onQrDetectado: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val imagen = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(imagen)
            .addOnSuccessListener { codigos ->
                codigos.firstOrNull()?.rawValue?.let(onQrDetectado)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

@Composable
fun EscanerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val factory = remember {
        EscanerViewModel.factory(
            RepositoryProvider.productoRepository(context),
            RepositoryProvider.localRepository(context)
        )
    }
    val vm: EscanerViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    // ---- Permiso de cámara con ActivityResultLauncher ----
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    LaunchedEffect(Unit) {
        if (!tienePermiso) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    // El resultado se oculta solo después de 2,5 segundos
    LaunchedEffect(estado.resultado) {
        if (estado.resultado != null) {
            delay(2_500)
            vm.limpiarResultado()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (tienePermiso) {
                VistaCamara(onQrDetectado = vm::procesarLectura)
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("La app necesita la cámara para leer códigos QR.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) }) {
                        Text("Dar permiso")
                    }
                }
            }

            // Animación del resultado
            androidx.compose.animation.AnimatedVisibility(
                visible = estado.resultado != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                estado.resultado?.let { TarjetaResultado(it) }
            }
        }

        HorizontalDivider()

        // Historial del usuario activo
        Text(
            "Historial de lecturas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(estado.historial, key = { it.id }) { lectura ->
                Text(
                    "${lectura.contenido} · ${lectura.tipo} · ${formatoFecha.format(Date(lectura.fechaLectura))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun VistaCamara(onQrDetectado: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val proveedorFuture = ProcessCameraProvider.getInstance(ctx)

            proveedorFuture.addListener({
                val proveedor = proveedorFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(ctx), QrAnalyzer(onQrDetectado))
                    }

                proveedor.unbindAll()
                proveedor.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )

    // Libera la cámara al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose { ProcessCameraProvider.getInstance(context).get().unbindAll() }
    }
}

@Composable
private fun TarjetaResultado(resultado: ResultadoEscaneo) {
    val (icono, color, titulo, detalle) = when (resultado) {
        is ResultadoEscaneo.Exito ->
            listOf("✓", "#2E7D32", "¡Código registrado!", resultado.qr.descripcion)
        is ResultadoEscaneo.Duplicado ->
            listOf("!", "#F9A825", "Ya escaneaste este código", resultado.qr.descripcion)
        is ResultadoEscaneo.Invalido ->
            listOf("✕", "#C62828", "Código no válido", resultado.contenido)
        ResultadoEscaneo.SinSesion ->
            listOf("?", "#546E7A", "Inicia sesión", "Necesitas un usuario para guardar lecturas")
    }

    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.95f), MaterialTheme.shapes.large)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(android.graphics.Color.parseColor(color)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icono, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        Text(detalle, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}