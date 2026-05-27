package com.example.aia_solutions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import data.firebase.firebase.FirestoreService
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class QRScannerFragment : Fragment(R.layout.fragment_qr_scanner) {

    private val firestoreService = FirestoreService()
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner = BarcodeScanning.getClient(scannerOptions)
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            this.cameraProvider = cameraProvider
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage == null || hasScanned) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val image =
                            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                processBarcodes(barcodes)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processBarcodes(barcodes: List<Barcode>) {
        if (hasScanned) return
        val rawValue = barcodes.firstOrNull()?.rawValue ?: return
        val businessId = extractBusinessId(rawValue)
        if (businessId.isNullOrBlank()) return

        hasScanned = true
        firestoreService.obtenerNegocioPorId(businessId) { business ->
            activity?.runOnUiThread {
                if (!isAdded) return@runOnUiThread
                if (business == null) {
                    hasScanned = false
                    Toast.makeText(requireContext(), "QR inválido: negocio no encontrado", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
        scanner.close()
        cameraExecutor.shutdown()
    }
}

internal fun extractBusinessId(rawValue: String): String? {
    val value = rawValue.trim()
    if (value.isEmpty()) return null

    if (value.startsWith("aia://business/")) {
        return value.removePrefix("aia://business/").ifBlank { null }
    }

    val businessIdParam = Regex("""[?&]businessId=([^&#]+)""").find(value)?.groupValues?.getOrNull(1)
    if (!businessIdParam.isNullOrBlank()) return businessIdParam

    val idParam = Regex("""[?&]id=([^&#]+)""").find(value)?.groupValues?.getOrNull(1)
    if (!idParam.isNullOrBlank()) return idParam

    if (value.startsWith("http://") || value.startsWith("https://")) {
        val path = value.substringBefore('?').substringBefore('#')
        val lastSegment = path.substringAfterLast('/', "")
        if (lastSegment.isNotBlank()) return lastSegment
    }

    return value
}
