package com.sumedh.fileuploadretrofit

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    var selectedFileText: TextView? = null
    var progressBar: ProgressBar?= null
    var downloadButton: Button?= null
    var pickFileButton: Button?=null
    var uploadButton: Button?=null

    private val fileUploadService = RetrofitClient.instance
    private var selectedPdfUri: Uri? = null

    var downloadedURL:String? = null
    var selectedFileName:String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById<ProgressBar>(R.id.progressBar)

        selectedFileText = findViewById<TextView>(R.id.selectedFileName)

        pickFileButton = findViewById<Button>(R.id.pickFileButton)
        pickFileButton?.setOnClickListener {
            getContent.launch("application/pdf")
        }

        uploadButton = findViewById<Button>(R.id.uploadButton)
        uploadButton?.setOnClickListener {
            val pdfFile = selectedPdfUri?.let { it1 -> uriToFile(this, it1) }
            if (pdfFile != null) {
                progressBar?.visibility = View.VISIBLE
                uploadPDF(pdfFile)
            } else {
                Toast.makeText(this, "Please Select file to upload", Toast.LENGTH_LONG).show()
            }
        }

        downloadButton = findViewById<Button>(R.id.downloadButton)
        downloadButton?.setOnClickListener {
            val downloader = selectedFileName?.let { it1 -> FileDownloader(it1, this) }
            downloadedURL?.let { it1 -> downloader?.downloadFile(it1) }
        }
    }


    private fun uriToFile(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            try {
                val file = File(context.cacheDir, selectedFileName)
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                return file
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = uri
            selectedFileName = getFileName(uri)
            selectedFileText?.text = selectedFileName.toString()
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            val nameIndex = it.getColumnIndex("_display_name")
            it.moveToFirst()
            val fileName = it.getString(nameIndex)
            it.close()
            return fileName
        }
        return "unknown.pdf"
    }

    private fun uploadPDF(pdfFile: File) {
        val requestFile = RequestBody.create(MediaType.parse("application/pdf"), pdfFile)
        val pdfRequestBody = MultipartBody.Part.createFormData("file", pdfFile.name, requestFile)
//        val name = RequestBody.create(MultipartBody.FORM, "Sumedh")
//        val contactNumber = RequestBody.create(MultipartBody.FORM, "9021066696")

        selectedFileName?.let {
            fileUploadService.uploadFile(pdfRequestBody, it)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        progressBar?.visibility = View.GONE
                        if (response.isSuccessful) {
                            downloadedURL = response.body().toString()
                            downloadButton?.visibility = View.VISIBLE
                            selectedPdfUri = null
                            selectedFileName = ""
                            selectedFileText?.text = ""
                            Toast.makeText(this@MainActivity, "File Uploaded successfully...", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Error in File Upload...", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        progressBar?.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Error in File Upload...", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}