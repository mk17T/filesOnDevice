package com.duck.filesondevice


import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.duck.filesondevice.databinding.ActivityMainBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.newDirectoryStream
import java.nio.file.Paths
import kotlin.streams.toList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var folderList: ArrayList<Folder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnExt.setOnClickListener{
            val state = Environment.getExternalStorageState()
            if (state== Environment.MEDIA_MOUNTED) {
                val rootext =File(Environment.getExternalStorageDirectory().toString() +"/")
                binding.txtResults.append("\nrootExt:${rootext}" )
                try {
                    searchFiles(rootext, "jpg")
                } catch (ex:java.lang.Exception){
                    binding.txtResults.append("\nexceptionExt: ${ex.toString()}" )
                }

            }
        }

        binding.btnInternal.setOnClickListener{
            val root = applicationContext.filesDir

            binding.txtResults.append("\nroot:${root}" )
            try {
                searchFiles(root, "jpg")
            } catch (ex:java.lang.Exception){
                binding.txtResults.append("\nexception: ${ex.toString()}" )
            }

        }

        binding.btnClear.setOnClickListener { binding.txtResults.text=""
            if (ContextCompat.checkSelfPermission(this, MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                binding.txtResults.append("\n permission granted")
                //ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),1)
                val dir = Environment.getExternalStorageDirectory()
                if (dir.exists() && dir.isDirectory) {
                    binding.txtResults.append("\n exists : ${dir.toString()}")
                    val directories =  Files.walk(Paths.get(dir.toString()), Int.MAX_VALUE)
                        .filter { Files.isDirectory(it) }.toList()
                    binding.txtResults.append("\n${directories}")
                }
            }else binding.txtResults.append("\ndisplay permission never")
        }

    }


    private fun displayNeverAskAgainDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(
            """
            Need storage permission to select Pfp
            
            Select Permissions -> Enable permission
            """.trimIndent()
        )
        builder.setCancelable(false)
        builder.setPositiveButton(
            "Allow"
        ) { dialog, which ->
            dialog.dismiss()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun searchFilesHelper(file: File, fileExtension: String, matchingFiles: ArrayList<String>) {
        if (file.isDirectory) {
           binding.txtResults.append("\nDir: ${file}")
            for (childFile in file.listFiles()!!) {
                searchFilesHelper(childFile, fileExtension, matchingFiles)
                binding.txtResults.append("\nchilds:"+childFile.toString())
            }
        } else if (file.name.endsWith(".$fileExtension")) {
            matchingFiles.add(file.absolutePath)
        }
    }

    private fun searchFiles(root:File,fileExtension: String) {

        val matchingFiles = ArrayList<String>()

        searchFilesHelper(root, fileExtension, matchingFiles)

        if (matchingFiles.isEmpty()) {
            Toast.makeText(this, "No files match the search criteria", Toast.LENGTH_SHORT).show()
        } else {
            val listView = findViewById<ListView>(R.id.list_view)
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matchingFiles)
            listView.adapter = adapter
        }
    }




}