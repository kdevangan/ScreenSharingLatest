package com.ezmall.screensharinglatest_agora

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*

import androidx.core.app.NotificationCompat
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout


class FloatingWindowApp : Service()/*,View.OnClickListener  */{

   /* *//*override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }*//*

    private var mDemoView: View?=null;
//    private var mEndButton:Button?=null;

    private val TAG: String?="DemoService"
    private val mLayoutParamFlags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")
//        showNotification();

        return START_STICKY;
    }

//    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: ")
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDemoView = layoutInflater.inflate(R.layout.service_layout, null)

 *//*       val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
            mLayoutParamFlags,
            PixelFormat.TRANSLUCENT
        )*//*
    var parameters = WindowManager.LayoutParams(
        500,
        500,
        WindowManager.LayoutParams.TYPE_TOAST,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )

//        mEndButton = mDemoView?.findViewById(R.id.btn_join)
//        mEndButton?.setOnClickListener(this)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(mDemoView, parameters)
    }


    override fun onBind(p0: Intent?): IBinder? {
        Log.i(TAG, "onBind: ")
        return null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_join -> endService()
        }
    }
    private fun showNotification() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
//        startForeground(R.string.app_name, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove notification
        // Remove notification
        stopForeground(true)
        // Remove WindowManager
        // Remove WindowManager
//        if (mDemoView != null) {
//            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
//            wm.removeView(mDemoView)
        }
    private fun endService() {
        // Stop demo service
//        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }*/

    private lateinit var floatView:ViewGroup
    private lateinit var floatWindowLayoutParams:WindowManager.LayoutParams
    private var LAYOUT_TYPE:Int?=null
    private lateinit var windowManager: WindowManager
    private lateinit var btnStopCasting:Button
    private lateinit var cl_transparent:ConstraintLayout
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
/*        val metrix= applicationContext.resources.displayMetrics
        val width = metrix.widthPixels
        val height = metrix.heightPixels*/

        windowManager=getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater= baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatView=inflater.inflate(R.layout.service_layout,null ) as ViewGroup

        btnStopCasting=floatView.findViewById(R.id.btn_stop_casting)
        cl_transparent=floatView.findViewById(R.id.cl_transparent)

        cl_transparent.isClickable=false
//        cl_transparent.isFocusable=true

        cl_transparent.setOnTouchListener { p0, p1 -> true }

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            LAYOUT_TYPE=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
 }
        else{
            LAYOUT_TYPE=WindowManager.LayoutParams.TYPE_TOAST
        }

        floatWindowLayoutParams=WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        LAYOUT_TYPE!!,
            /*WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or*/ WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

      /*  floatWindowLayoutParams.gravity=Gravity.TOP or Gravity.RIGHT
        floatWindowLayoutParams.x=0
        floatWindowLayoutParams.y=0*/

        windowManager.addView(floatView,floatWindowLayoutParams)

        btnStopCasting.setOnClickListener {
            stopSelf()

            windowManager.removeView(floatView)

//            val back=Intent(this@FloatingWindowApp,MainActivity::class.java)
//            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(back)
            sendBroadcast()
        }


 /*       btnStopCasting.setOnTouchListener(
            object :View.OnTouchListener{
                val updatedFloatWindowLayoutParams=floatWindowLayoutParams
                var x=0.0;
                var y=0.0;
                var px=0.0;
                var py=0.0;
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    when(event?.action){
                        MotionEvent.ACTION_DOWN -> {
                            x=updatedFloatWindowLayoutParams.x.toDouble()
                            y=updatedFloatWindowLayoutParams.y.toDouble()

                            px=event.rawX.toDouble()
                            py=event.rawY.toDouble()
                        }
                        MotionEvent.ACTION_MOVE->{
                            updatedFloatWindowLayoutParams.x=(x+event.rawX-px).toInt()
                            updatedFloatWindowLayoutParams.y=(y+event.rawY-py).toInt()

                            windowManager.updateViewLayout(floatView,updatedFloatWindowLayoutParams)
                        }
                    }
                    return false;
                }

            }  )
        cl_transparent.setOnTouchListener(
            object :View.OnTouchListener{
                val updatedFloatWindowLayoutParams=floatWindowLayoutParams
                var x=0.0;
                var y=0.0;
                var px=0.0;
                var py=0.0;
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    when(event?.action){
                        MotionEvent.ACTION_DOWN -> {
                            x=updatedFloatWindowLayoutParams.x.toDouble()
                            y=updatedFloatWindowLayoutParams.y.toDouble()

                            px=event.rawX.toDouble()
                            py=event.rawY.toDouble()
                        }
                        MotionEvent.ACTION_MOVE->{
                            updatedFloatWindowLayoutParams.x=(x+event.rawX-px).toInt()
                            updatedFloatWindowLayoutParams.y=(y+event.rawY-py).toInt()

                            windowManager.updateViewLayout(floatView,updatedFloatWindowLayoutParams)
                        }
                    }
                    return false;
                }

            }  )*/

        floatView.setOnTouchListener(object :View.OnTouchListener{
            val updatedFloatWindowLayoutParams=floatWindowLayoutParams
            var x=0.0;
            var y=0.0;
            var px=0.0;
            var py=0.0;
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when(event?.action){
                    MotionEvent.ACTION_DOWN -> {
                        x=updatedFloatWindowLayoutParams.x.toDouble()
                        y=updatedFloatWindowLayoutParams.y.toDouble()

                        px=event.rawX.toDouble()
                        py=event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE->{
                        updatedFloatWindowLayoutParams.x=(x+event.rawX-px).toInt()
                        updatedFloatWindowLayoutParams.y=(y+event.rawY-py).toInt()

                        windowManager.updateViewLayout(floatView,updatedFloatWindowLayoutParams)
                    }
                }
                return false;
            }

        })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        windowManager.removeView(floatView)
    }

    fun sendBroadcast() {
        val broadcast = Intent()
        broadcast.action = "BROADCAST_ACTION"
        sendBroadcast(broadcast)
    }
    }



