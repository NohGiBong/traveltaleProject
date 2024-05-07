package com.example.traveltaleproject.user

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.traveltaleproject.BottomNavigationHelper
import com.example.traveltaleproject.LoginActivity

import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityMypageBinding
import com.example.traveltaleproject.models.Member
import com.example.traveltaleproject.travellist.TravelListActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMypageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var selectedImageUri: Uri
    private var MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)

        // GoogleSignInClient 초기화
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        // Firebase Realtime Database 초기화
        val databaseReference = FirebaseDatabase.getInstance().reference

        // 사용자 정보 가져오기
        val userId = sharedPreferences.getString("user_id", "")

        if (!userId.isNullOrEmpty()) {
            getInfoFromDB(userId, databaseReference)
        }

        // 프로필 사진 변경
        binding.mypageProfile.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.RoundedCornersDialog)
            builder.setTitle("프로필 사진을 변경하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    changeProfileImage()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(resources.getColor(R.color.black))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(resources.getColor(R.color.black))
            }

            dialog.show()
        }

        // My TravelTale 버튼 클릭
        binding.mypageTravelBtn.setOnClickListener {
            val intent = Intent(this, TravelListActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭
        binding.mypageLogout.setOnClickListener {
            confirmLogout()
        }

        // 개인 정보 관리 버튼 클릭
        binding.myinfoBtn.setOnClickListener {
            val intent = Intent(this, MyInfoActivity::class.java)
            val userId = sharedPreferences.getString("user_id", "")
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        // 고객센터 버튼 클릭
        binding.mypageCenterBtn.setOnClickListener {
            toggleView()
        }

        // 개인정보처리방침 클릭
        binding.mypagePrivacy.setOnClickListener {
            val intent = Intent(this, MyPrivacyPolicy::class.java)
            startActivity(intent)
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }

    private fun changeProfileImage() {
        if (ContextCompat.checkSelfPermission(this@MyPageActivity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없다면 사용자에게 권한 요청
            ActivityCompat.requestPermissions(this@MyPageActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        } else {
            // 권한이 이미 허용되었다면 앨범 열기
            openGallery()
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { selectedImageUri ->
                this.selectedImageUri = selectedImageUri
                val inputStream = contentResolver.openInputStream(selectedImageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val circularBitmap = getCircularBitmap(bitmap)
                val bitmapDrawable = BitmapDrawable(resources, circularBitmap)
                binding.mypageProfile.setImageDrawable(bitmapDrawable)
                saveProfileImage()
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

    private fun saveProfileImage() {
        val userId = sharedPreferences.getString("user_id", "")
        if (!userId.isNullOrEmpty()) {
            // Firebase Storage에 이미지 업로드
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profile_images/$userId/$userId.jpg")

            // 선택한 이미지의 Uri를 사용하여 이미지 업로드
            selectedImageUri?.let { uri ->
                imageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        // 업로드 성공 시 다운로드 URL 가져오기
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            // Realtime Database에 다운로드 URL 저장
                            val databaseReference = FirebaseDatabase.getInstance().reference
                            val userRef = databaseReference.child("Member").child(userId)
                            userRef.child("profileImage").setValue(downloadUrl)
                                .addOnSuccessListener {
                                    showToast("프로필 사진이 성공적으로 변경되었습니다.")
                                }
                                .addOnFailureListener { e ->
                                    showToast("프로필 사진 변경에 실패했습니다: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("이미지 업로드에 실패했습니다: ${e.message}")
                    }
            }
        } else {
            showToast("사용자 ID가 없습니다.")
        }
    }

    private fun confirmLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
                loginTypeConfirm()
            }
            .setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int ->
                // 취소 버튼 클릭 시 아무 동작 없음
            }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.black))
        }

        dialog.show()
    }

    private fun loginTypeConfirm() {
        val loginType = sharedPreferences.getString("login_type", "")

        when (loginType) {
            "normal" -> {
                normalLogout()
            }

            "naver" -> {
                naverLogout()
            }

            "google" -> {
                googleLogout()
            }

            "kakao" -> {
                kakaoLogout()
            }

            else -> { showToast("LOGIN TYPE 식별 불가")}
        }
    }

    private fun normalLogout() {
        deleteSession()
        moveToLoginActivity()
        showToast("로그아웃하였습니다 🐳")
    }

    private fun naverLogout() {
        NaverIdLoginSDK.logout()
        deleteSession()
        moveToLoginActivity()
        showToast("로그아웃하였습니다 🐳")
    }

    private fun kakaoLogout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("TAG", "카카오 로그아웃 실패", error)
            } else {
                deleteSession()
                moveToLoginActivity()
                showToast("로그아웃하였습니다 🐳")
            }
        }
    }

    private fun googleLogout() {
        googleSignInClient.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                deleteSession()
                moveToLoginActivity()
                showToast("로그아웃하였습니다 🐳")
            } else {
                // 로그아웃 실패 처리
            }
        }
    }

    private fun deleteSession() {
        val editor = sharedPreferences.edit()
        editor.remove("user_id")
        editor.remove("login_type")
        editor.apply()
    }

    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }

    private fun getInfoFromDB(userId: String, databaseReference: DatabaseReference) {
        val userRef = databaseReference.child("Member").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Member::class.java)
                // 사용자 정보가 null이 아닌 경우에만 닉네임 설정
                if (user != null) {
                    val userNickname = user.nickname
                    val userProfileImage = user.profileImage
                    if (!userNickname.isNullOrEmpty()) {
                        binding.mypageNickname.text = userNickname
                        if (userProfileImage.isNullOrEmpty()) {
                            // 사용자의 프로필 이미지가 없을 때 기본 이미지 설정
                            binding.mypageProfile.setImageResource(R.drawable.profile)
                        } else {
                            // 사용자의 프로필 이미지를 Picasso를 사용하여 불러옴
                            GlobalScope.launch(Dispatchers.Main) {
                                val bitmap = withContext(Dispatchers.IO) {
                                    try {
                                        Picasso.get().load(userProfileImage).get()
                                    } catch (e: IOException) {
                                        null
                                    }
                                }

                                bitmap?.let {
                                    val circularBitmap = getCircularBitmap(it)
                                    binding.mypageProfile.setImageBitmap(circularBitmap)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("dbError", "DB에서 가져오지 못함")
            }
        })
    }

    private fun toggleView() {
        if(binding.mypageCenterTxt.visibility == View.GONE) {
            binding.mypageCenterTxt.visibility = View.VISIBLE
            binding.mypageArrow.setBackgroundResource(R.drawable.grey_arrow_under)
        }
        else {
            binding.mypageCenterTxt.visibility = View.GONE
            binding.mypageArrow.setBackgroundResource(R.drawable.grey_arrow_right)

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getCircularBitmap(srcBitmap: Bitmap): Bitmap {
        val squareBitmap = if (srcBitmap.width >= srcBitmap.height) {
            Bitmap.createBitmap(
                srcBitmap,
                srcBitmap.width / 2 - srcBitmap.height / 2,
                0,
                srcBitmap.height,
                srcBitmap.height
            )
        } else {
            Bitmap.createBitmap(
                srcBitmap,
                0,
                srcBitmap.height / 2 - srcBitmap.width / 2,
                srcBitmap.width,
                srcBitmap.width
            )
        }

        val outputBitmap = Bitmap.createBitmap(squareBitmap.width, squareBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        val rect = Rect(0, 0, squareBitmap.width, squareBitmap.height)
        val rectF = RectF(rect)
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(
            squareBitmap.width.toFloat() / 2,
            squareBitmap.height.toFloat() / 2,
            squareBitmap.width.toFloat() / 2,
            paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squareBitmap, rect, rectF, paint)
        squareBitmap.recycle()

        return outputBitmap
    }
}

