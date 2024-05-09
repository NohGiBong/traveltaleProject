package com.example.traveltaleproject.travellist

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.Locale

class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var isButtonVisible = false
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_googlemap)

        // API 키를 사용하여 Places 초기화
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        // 지도를 로드하고 사용할 준비가 되었을 때 알림을 받습니다.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 검색한 주소로 이동 버튼 클릭 시 이벤트 처리

        // AutocompleteSupportFragment에 이벤트 리스너 추가
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.map_search) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val location = place.latLng
                if (location != null) {
                    mMap.clear() // 이전 마커를 모두 지웁니다.
                    mMap.addMarker(MarkerOptions().position(location)) // 선택한 위치에 마커 추가
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            location,
                            15f
                        )
                    ) // 해당 위치로 지도 이동
                    showToast("$location")
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(
                    this@GoogleMapActivity,
                    "Place selection failed: ${status.statusMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener { marker ->
            if (isButtonVisible) {
                removeSaveButton() // 이미 버튼이 있는 경우 삭제
            } else {
                addSaveButton(marker.position) // 버튼이 없는 경우 추가
            }
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addSaveButton(position: LatLng) {
        saveButton = Button(this)
        saveButton.text = "Save"

        isButtonVisible = true

        // 마커의 위치를 픽셀 좌표로 변환하여 마커 위로 버튼을 이동시킵니다.
        val markerPosition = mMap.projection.toScreenLocation(position)
        saveButton.x = (markerPosition.x - dpToPx(50)).toFloat() // 가로 마진 설정
        saveButton.y = (markerPosition.y - dpToPx(100)).toFloat() // 세로 마진 설정

        saveButton.setOnClickListener {
            getRoadAddress(position)
        }

        window.addContentView(
            saveButton,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun getRoadAddress(position: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1)
        val address = addresses?.get(0)?.getAddressLine(0)

        // TravelAddActivity로 도로명 주소 전달
        val intent = Intent(this, TravelAddActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    private fun removeSaveButton() {
        if (isButtonVisible) {
            saveButton.visibility = View.INVISIBLE
            isButtonVisible = false
        }
    }

    // dp를 픽셀로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

