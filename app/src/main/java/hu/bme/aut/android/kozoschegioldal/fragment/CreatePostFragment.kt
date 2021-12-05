package hu.bme.aut.android.kozoschegioldal.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import hu.bme.aut.android.kozoschegioldal.R
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentCreatePostBinding
import hu.bme.aut.android.kozoschegioldal.interfaces.Callback
import hu.bme.aut.android.kozoschegioldal.model.Post
import hu.bme.aut.android.kozoschegioldal.viewmodel.PostViewModel
import permissions.dispatcher.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@RuntimePermissions
class CreatePostFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by activityViewModels()

    private val pickImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { it ->
            Glide.with(this).load(it).into(binding.ivCreatePostImage)
            binding.ivCreatePostImage.visibility = View.VISIBLE
        }
    }

    private var uri: Uri? = null

    private val takePictureFromCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Glide.with(this).load(uri).into(binding.ivCreatePostImage)
            binding.ivCreatePostImage.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)

        binding.btnFromGallery.setOnClickListener {
            pickImageFromGallery.launch("image/*")
        }
        binding.btnFromCamera.setOnClickListener {
            takePictureWithPermissionCheck()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_create_post, menu)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnCreatePost -> {
                if (binding.etPostText.text.toString().isNotEmpty() || binding.ivCreatePostImage.drawable != null) {
                    val callback = object : Callback {
                        override fun onSuccess() {
                            findNavController().popBackStack()
                        }

                        override fun onFailure(t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to create post. Try again later!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                    if (binding.ivCreatePostImage.drawable != null) {
                        val bitmap = (binding.ivCreatePostImage.drawable as BitmapDrawable).bitmap
                        if (bitmap != null) {
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val data = baos.toByteArray()
                            postViewModel.createPost(
                                callback,
                                binding.etPostText.text.toString(),
                                data
                            )
                        }
                    } else {
                        postViewModel.createPost(callback, binding.etPostText.text.toString())
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Cannot create empty post!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePicture() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(path, "/KozoSCHegiOldal/" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg")
        path.mkdirs()
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()))
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/KozoSCHegiOldal")
            } else {
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }

        }
        uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        takePictureFromCamera.launch(uri)
        Log.d("Uri", uri.toString())
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePictureDenied() {
        Toast.makeText(requireContext(), "Storage access denied", Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePictureRationale(request: PermissionRequest) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Needs access")
            .setMessage("Needs access to storage to save the taken picture.")
            .setCancelable(false)
            .setPositiveButton("Allow") { _, _ -> request.proceed() }
            .setNegativeButton("Deny") { _, _ -> request.cancel() }
            .create()
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}