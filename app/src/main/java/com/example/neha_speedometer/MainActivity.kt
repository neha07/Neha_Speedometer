package com.example.neha_speedometer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.properties.Delegates

private fun FusedLocationProviderClient.removeLocationUpdates(locationRequest: LocationRequest, locationCallback: LocationCallback, mainLooper: Looper?) {

}

class MainActivity : Activity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private val TAG = "MainActivity"
    private val LOCATION_PERM = 124
    private var speedUpStartTime = 0L
    private var speedUpEndTime = 0L
    private var speedDownStartTime = 0L
    private var speedDownEndTime = 0L
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isDone: Boolean by Delegates.observable(false){property, oldValue, newValue ->
        if(newValue == true){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
        
    }

    private fun calcSpeed(speed: Int) {
        val thirtyToTenIdTextView: TextView = findViewById(R.id.thirtyToTenId) as TextView

        if(speed >= 10){
            speedUpStartTime = System.currentTimeMillis()
            speedDownEndTime = System.currentTimeMillis()



            if(speedDownStartTime != 0L){
                val speedDownTime = speedDownEndTime - speedDownStartTime
                thirtyToTenIdTextView.text = (speedDownTime/1000).toString()
                speedDownStartTime = 0L
            }
        }
        else if(speed >= 30){

            if(speedUpStartTime != 0L){
                speedUpEndTime = System.currentTimeMillis()
                val speedUpTime = speedUpEndTime - speedUpStartTime
                thirtyToTenIdTextView.text = (speedUpTime/1000).toString()
                speedUpStartTime = 0L
            }
            speedDownStartTime = System.currentTimeMillis()
        }

    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        askForLocationPermission()
        createLocationRequest()
        val currentSpeedIdTextView: TextView = findViewById(R.id.currentSpeedId) as TextView

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                if(!isDone){
                    val speedToInt = locationResult.lastLocation?.speed?.toInt()
                    speedToInt?.let { calcSpeed(it) }
                    currentSpeedIdTextView.text = speedToInt.toString()
                }
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {

        return EasyPermissions.hasPermissions(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun askForLocationPermission() {

        if(hasLocationPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? -> }
               }else{
                    EasyPermissions.requestPermissions(this,
                        "need permissions to find your location and calculate the speed",
                        LOCATION_PERM,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }


    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateDistanceMeters(0.05F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }

    override fun onRationaleDenied(requestCode: Int) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            val yes = "Allow"
            val no = "Deny"
            Toast.makeText(this,"onActivityResult",Toast.LENGTH_LONG).show()
        }

    }
}

