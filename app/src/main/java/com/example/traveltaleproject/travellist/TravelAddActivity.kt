package com.example.traveltaleproject.travellist

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.NumberPickerBindingAdapter.setValue
import com.example.traveltaleproject.databinding.ActivityTraveladdBinding
import com.example.traveltaleproject.models.TravelList
import com.example.traveltaleproject.user.MyPageActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


class TravelAddActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityTraveladdBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var selectedImageUri: Uri
    private var MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101

    private var startDateIntent: Long? = null
    private var endDateIntent: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTraveladdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")

        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList")

        binding.apply {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val startDateInMillis = it.first ?: return@addOnPositiveButtonClickListener
                startDateIntent = startDateInMillis
                val endDateInMillis = it.second ?: return@addOnPositiveButtonClickListener
                endDateIntent = endDateInMillis

                val sdf = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH)

                val startDate = Calendar.getInstance().apply { timeInMillis = startDateInMillis }
                val endDate = Calendar.getInstance().apply { timeInMillis = endDateInMillis }

                val formattedStartDate = sdf.format(startDate.time)
                showToast(formattedStartDate)
                val formattedEndDate = sdf.format(endDate.time)

                dateTxtLayout.visibility = View.GONE
                // selected_date_txt_layout을 visible로 설정
                selectedDateTxtLayout.visibility = View.VISIBLE
                // selected_date_txt_layout이 visible로 설정된 후
                val layoutParams = dateBtn.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.RIGHT_OF, selectedDateTxtLayout.id)
                dateBtn.layoutParams = layoutParams

                startDateTxt.text = (formattedStartDate)
                endDateTxt.text = (formattedEndDate)
            }

            mapBtn.setOnClickListener {
                val intent = Intent(this@TravelAddActivity, GoogleMapActivity::class.java)
                startActivity(intent)
            }

            dateBtn.setOnClickListener {
                picker.show(supportFragmentManager, picker.toString())
            }

            imgAdd.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@TravelAddActivity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없다면 사용자에게 권한 요청
                    ActivityCompat.requestPermissions(this@TravelAddActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                } else {
                    // 권한이 이미 허용되었다면 앨범 열기
                    openGallery()
                }
            }

            val address = intent.getStringExtra("address")
            val editableAddress = address?.let { Editable.Factory.getInstance().newEditable(it) }
            mapText.text = editableAddress

            submitBtn.setOnClickListener {
                saveTravelList()
            }
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { selectedImageUri ->
                this.selectedImageUri = selectedImageUri
                val inputStream = contentResolver.openInputStream(selectedImageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val bitmapDrawable = BitmapDrawable(resources, bitmap)
                binding.addMainImg.background = bitmapDrawable
            } ?: run {
                Toast.makeText(this, "사진을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "갤러리 접근을 위해 권한이 필요합니다. 설정 > 애플리케이션 > 권한에서 설정해주세요.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startForResult.launch(galleryIntent)
    }

    private fun saveTravelList() {
        val title = binding.inputTitle.text.toString().trim()
        val date = binding.selectedDateTxtLayout.toString().trim()
        val userId = sharedPreferences.getString("user_id", "")
        val address = binding.mapText.text.toString().trim()
        val startDateIntent = startDateIntent
        val endDateIntent = endDateIntent

        if (title.isNotEmpty() && date.isNotEmpty() && ::selectedImageUri.isInitialized) {
            val startDate = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH).format(startDateIntent)
            val endDate = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH).format(endDateIntent)

            val travelListId = UUID.randomUUID().toString()
            val imageName = "travel_lists/$userId//$travelListId.jpg"
            val travel = TravelList(travelListId, title, "$startDate - $endDate", address, imageName, startDateIntent ?: 0L, endDateIntent ?: 0L)

            // Firebase Storage에 이미지 업로드
            val storageReference = FirebaseStorage.getInstance().reference.child(imageName)
            storageReference.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // 이미지 업로드 성공 시, 다운로드 URL 가져오기
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        travel.travelImage = imageUrl

                        // Realtime Database에 데이터 저장
                        if (userId != null) {
                            databaseReference.child(userId).child(travelListId).setValue(travel)
                                .addOnSuccessListener {
                                    showToast("여행 리스트 저장 완료")
                                    startTravelListActivity()
                                }
                                .addOnFailureListener{
                                    showToast("여행 리스트 저장 실패")
                                }
                        } else {
                            showToast("제목과 날짜를 입력하세요")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("이미지 업로드 실패: ${exception.message}")
                }
        } else {
            showToast("제목, 날짜, 이미지를 선택하세요")
        }
    }

    private fun startTravelListActivity() {
        val intent = Intent(this, TravelListActivity::class.java)
        intent.putExtra("startDateIntent", startDateIntent)
        intent.putExtra("endDateIntent", endDateIntent)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }
}