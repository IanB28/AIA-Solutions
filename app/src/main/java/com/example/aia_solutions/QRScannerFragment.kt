package com.example.aia_solutions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import data.firebase.firebase.FirestoreService
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

class QRScannerFragment : Fragment(R.layout.fragment_qr_scanner) {

    private val firestoreService = FirestoreService()
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner: BarcodeScanner by lazy { BarcodeScanning.getClient(scannerOptions) }
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private var hasScanned = false
    private var cameraProvider: ProcessCameraProvider? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara requerido", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasCameraPermission()) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val previewView = view?.findViewById<PreviewView>(R.id.previewView) ?: return
        
        // Intentamos obtener la instancia del proveedor de la cámara
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = 
            ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                // Obtenemos el proveedor (usamos casting explícito por seguridad)
                val provider = cameraProviderFuture.get() as ProcessCameraProvider
                this.cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy)
                        }
                    }

                provider.unbindAll()
                provider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || hasScanned) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage, 
            imageProxy.imageInfo.rotationDegrees
        )
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    processBarcodes(barcodes)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processBarcodes(barcodes: List<Barcode>) {
        if (hasScanned) return
        val rawValue = barcodes.firstOrNull()?.rawValue ?: return
        val businessId = extractBusinessId(rawValue)

        if (businessId.isNullOrBlank()) return

        hasScanned = true
        firestoreService.obtenerNegocioPorId(businessId) { business ->
            if (!isAdded || view == null) return@obtenerNegocioPorId
            
            if (business == null) {
                hasScanned = false
                Toast.makeText(requireContext(), "Negocio no encontrado", Toast.LENGTH_SHORT).show()
                return@obtenerNegocioPorId
            }

            Toast.makeText(requireContext(), "Negocio detectado: ${business.name}", Toast.LENGTH_SHORT).show()
            
            val detailFragment = TurnoDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("businessId", business.id)
                    putString("businessName", business.name)
                }
            }
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentos, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
        cameraProvider = null
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        scanner.close()
        super.onDestroy()
    }
}

/**
 * Extrae el ID del negocio de diferentes formatos de QR sin causar conflictos de tipos
 */
internal fun extractBusinessId(rawValue: String): String? {
    val value = rawValue.trim()
    if (value.isEmpty()) return null

    // 1. Esquema personalizado
    if (value.startsWith("aia://business/")) {
        return value.removePrefix("aia://business/").substringBefore("/").ifBlank { null }
    }

    // 2. Parámetros de URL (usamos nombres de variables distintos para evitar shadowing)
    val businessMatch = Regex("""[?&]businessId=([^&#]+)""").find(value)
    if (businessMatch != null) return businessMatch.groupValues.getOrNull(1)

    val idMatch = Regex("""[?&]id=([^&#]+)""").find(value)
    if (idMatch != null) return idMatch.groupValues.getOrNull(1)

    // 3. Ruta de URL estándar
    if (value.startsWith("http")) {
        val path = value.substringBefore('?').substringBefore('#')
        if ("/business/" in path) {
            val idFromPath = path.substringAfter("/business/").substringBefore('/')
            if (idFromPath.isNotBlank()) return idFromPath
        }
    }

    // 4. ID Directo
    val directIdRegex = Regex("^[A-Za-z0-9_-]{3,100}$")
    return if (directIdRegex.matches(value)) value else null
}
