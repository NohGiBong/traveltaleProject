package com.example.traveltaleproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.user.MyPageActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationHelper(private val activity: AppCompatActivity, val context: Context) {

    private var currentActivityName: String = activity::class.java.simpleName

    fun setupBottomNavigationListener(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleMenuItemClick(menuItem.itemId)
        }
    }


    fun handleMenuItemClick(itemId: Int): Boolean {
        when (itemId) {
            R.id.home_item -> {
                if (currentActivityName != "GetActivity") {
                    // 현재 페이지가 GetActivity가 아니면 HomeActivity로 이동
                    val intent = Intent(activity, GetActivity::class.java)
                    activity.startActivity(intent)
//                    val toast = Toast.makeText(activity, currentActivityName, Toast.LENGTH_SHORT)
//                    toast.show()
                    return true
                }
            }
            R.id.mypage_item -> {
                if (currentActivityName != "MyPageActivity") {
                    // 현재 페이지가 MyPageActivity가 아니면 HomeActivity로 이동
                    val intent = Intent(activity, MyPageActivity::class.java)
                    activity.startActivity(intent)
//                    val toast = Toast.makeText(activity, currentActivityName, Toast.LENGTH_SHORT)
//                    toast.show()
                    return true
                }
            }
            R.id.back_item -> {
                // 뒤로 가기 버튼은 현재 액티비티를 종료하여 뒤로 가기 구현
                (context as? Activity)?.finish()
                return true
            }
        }
        return false
    }
}
