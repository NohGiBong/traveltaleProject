package com.example.traveltaleproject.tale

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityTaleGetBinding
import com.google.firebase.database.FirebaseDatabase

class TaleGetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityTaleGetBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val taleData = intent.getParcelableExtra<TaleData>("taleData")

        Log.d("bbbb", taleData.toString())

        binding.taleGet.text = taleData?.text?: ""

        // 삭제 후 write로 돌아가는 메서드
        fun goToWriteActivity() {
            val intent = Intent(this, TaleWriteActivity::class.java)
            startActivity(intent)
            finish()
        }

        with(binding) {
            menuBtn.setOnClickListener {
                val popupMenu = PopupMenu(this@TaleGetActivity, it)
                menuInflater.inflate(R.menu.tale_menu, popupMenu.menu)
                // 콜백 메서드 구현
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            val intent = Intent(this@TaleGetActivity, TaleEditActivity::class.java)
                            if (taleData != null) {
                                // 수정할 데이터의 고유 ID 전달
                                intent.putExtra("aid", taleData.aid)
                            }
                            startActivity(intent)
                            finish()

                            Toast.makeText(this@TaleGetActivity, "수정 클릭", Toast.LENGTH_SHORT).show()
                        }
                        R.id.action_delete -> {
                            // 데이터베이스에 저장된 aid를 기준으로 현재 불러온 게시물 삭제
                            val database = FirebaseDatabase.getInstance().reference.child("tales")
                            database.child(taleData?.aid.toString()).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@TaleGetActivity, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    goToWriteActivity() // 삭제 후 write로 돌아가는 메서드 호출
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@TaleGetActivity, "게시물 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    false
                }
                popupMenu.show()
            }
        }

    }
}
