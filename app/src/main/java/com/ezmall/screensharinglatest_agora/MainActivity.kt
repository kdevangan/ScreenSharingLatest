package com.ezmall.screensharinglatest_agora

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ezmall.screensharinglatest_agora.utils.CommonUtil
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.ScreenCaptureParameters
import io.agora.rtc.models.ChannelMediaOptions
import io.agora.rtc.video.VideoCanvas
import java.lang.Exception
import kotlin.math.abs
import android.widget.Toast

import android.content.BroadcastReceiver
import android.content.IntentFilter

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG: String = "MainActivity"
    private var handler: Handler? = null
    private var fl_remote: FrameLayout? = null
    private var join: Button? = null
    private var et_channel: EditText? = null
    private var description: TextView? = null
    private var engine: RtcEngine? = null
    private var myUid = 0
    private var joined = false
    private lateinit var dialog: AlertDialog;

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(applicationContext, "received", Toast.LENGTH_SHORT).show()
            findViewById<View>(R.id.btn_join).performClick()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        handler = Handler(Looper.getMainLooper())
        join = findViewById<Button>(R.id.btn_join)
        description = findViewById<TextView>(R.id.description)
        et_channel = findViewById<EditText>(R.id.et_channel)
        findViewById<View>(R.id.btn_join).setOnClickListener(this)
        fl_remote = findViewById<FrameLayout>(R.id.fl_remote)
        createRTCEngine()
        registerReceiver()


    }

    override fun onClick(v: View?) {

        if (v?.id == R.id.btn_join) {

            if (isSerivceRunning()){
                stopService(Intent(this@MainActivity,FloatingWindowApp::class.java))
            }

            if( checkOverlayPermission()){
                startService(Intent(this@MainActivity,FloatingWindowApp::class.java))
//                finish()
            }
            else{
                requestFloatingWindowPermission()
            }

//            startService(Intent(this,FloatingWindowApp::class.java))

            if (!joined) {
                et_channel?.let { CommonUtil.hideInputBoard(this, it) }
                // call when join button hit
                val channelId = et_channel?.getText().toString()
                // Check permission
                if ( checkSelfPermission()) {
                    joinChannel(channelId)
                    return
                }
                else{
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA
                        ),
                        1001
                    )
                }

            } else {
                joined = false
                /**After joining a channel, the user must call the leaveChannel method to end the
                 * call before joining another channel. This method returns 0 if the user leaves the
                 * channel and releases all resources related to the call. This method call is
                 * asynchronous, and the user has not exited the channel when the method call returns.
                 * Once the user leaves the channel, the SDK triggers the onLeaveChannel callback.
                 * A successful leaveChannel method call triggers the following callbacks:
                 * 1:The local client: onLeaveChannel.
                 * 2:The remote client: onUserOffline, if the user leaving the channel is in the
                 * Communication channel, or is a BROADCASTER in the Live Broadcast profile.
                 * @returns 0: Success.
                 * < 0: Failure.
                 * PS:
                 * 1:If you call the destroy method immediately after calling the leaveChannel
                 * method, the leaveChannel process interrupts, and the SDK does not trigger
                 * the onLeaveChannel callback.
                 * 2:If you call the leaveChannel method during CDN live streaming, the SDK
                 * triggers the removeInjectStreamUrl method.
                 */
                engine?.leaveChannel()
                join?.setText(getString(R.string.join))
            }
        }
    }


    private fun checkSelfPermission(): Boolean {
        Log.i(
            TAG, "checkSelfPermission"
        )
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED )
         {
            return false
        }
        return true
    }


    private fun joinChannel(channelId: String) {
        /** Sets the channel profile of the Agora RtcEngine.
         * CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile.
         * Use this profile in one-on-one calls or group calls, where all users can talk freely.
         * CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast
         * channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams;
         * an audience can only receive streams. */
        engine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        /**In the demo, the default is to enter as the anchor. */
        engine?.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER)
        // Enable video module
        engine?.enableVideo()


        engine?.startScreenCapture(getScreenCaptureParameters())

        description?.text = "Screen Sharing Starting"
        /**Please configure accessToken in the string_config file.
         * A temporary token generated in Console. A temporary token is valid for 24 hours. For details, see
         * https://docs.agora.io/en/Agora%20Platform/token?platform=All%20Platforms#get-a-temporary-token
         * A token generated at the server. This applies to scenarios with high-security requirements. For details, see
         * https://docs.agora.io/en/cloud-recording/token_server_java?platform=Java */
        var accessToken: String? = getString(R.string.agora_access_token)
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(
                accessToken,
                "<#YOUR ACCESS TOKEN#>"
            )
        ) {
            accessToken = null
        }
        /** Allows a user to join a channel.
         * if you do not specify the uid, we will generate the uid for you */
        val option = ChannelMediaOptions()
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        val res = engine?.joinChannel(accessToken, channelId, "Extra Optional Data", 1263, option)
        if (res != 0) {
            // Usually happens with invalid parameters
            // Error code description can be found at:
            // en: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
            // cn: https://docs.agora.io/cn/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
            res?.let {
                showAlert(RtcEngine.getErrorDescription(abs(it)))
            }
            return
        }
        // Prevent repeated entry
        join?.isEnabled = false
    }

    private fun showAlert(message: String?) {
        AlertDialog.Builder(this).setTitle("Tips").setMessage(message)
            .setPositiveButton(
                "OK"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            .show()
    }

    private fun createRTCEngine(){
        try {
            /**Creates an RtcEngine instance.
             * @param context The context of Android Activity
             * @param appId The App ID issued to you by Agora. See [
 * How to get the App ID](https://docs.agora.io/en/Agora%20Platform/token#get-an-app-id)
             * @param handler IRtcEngineEventHandler is an abstract class providing default implementation.
             * The SDK uses this class to report to the app on SDK runtime events.
             */
            engine = RtcEngine.create(
                applicationContext,
                getString(R.string.agora_app_id),
                iRtcEngineEventHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
            onBackPressed()
        }
    }

    /**
     * IRtcEngineEventHandler is an abstract class providing default implementation.
     * The SDK uses this class to report to the app on SDK runtime events.
     */
    private val iRtcEngineEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        /**Reports a warning during SDK runtime.
         * Warning code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_warn_code.html */
        override fun onWarning(warn: Int) {
            Log.w(
               TAG,
                String.format(
                    "onWarning code %d message %s",
                    warn,
                    RtcEngine.getErrorDescription(warn)
                )
            )
        }

        /**Reports an error during SDK runtime.
         * Error code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html */
        override fun onError(err: Int) {
            Log.e(
               TAG,
                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
            )
            showAlert(
                String.format(
                    "onError code %d message %s",
                    err,
                    RtcEngine.getErrorDescription(err)
                )
            )
        }

        /**Occurs when a user leaves the channel.
         * @param stats With this callback, the application retrieves the channel information,
         * such as the call duration and statistics.
         */
        override fun onLeaveChannel(stats: RtcStats) {
            super.onLeaveChannel(stats)
            Log.i(
               TAG,
                String.format("local user %d leaveChannel!", myUid)
            )
            showLongToast(String.format("local user %d leaveChannel!", myUid))
        }

        /**Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         * @param channel Channel name
         * @param uid User ID
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered
         */
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(
               TAG,
                String.format("onJoinChannelSuccess channel %s uid %d", channel, uid)
            )
            showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
            myUid = uid
            joined = true
            handler?.post(Runnable {
                join?.isEnabled = true
                join?.text = getString(R.string.leave)
            })
        }

        override fun onFirstLocalVideoFramePublished(elapsed: Int) {
            description!!.text = "Screen Sharing started"
        }

        /**Since v2.9.0.
         * This callback indicates the state change of the remote audio stream.
         * PS: This callback does not work properly when the number of users (in the Communication profile) or
         * broadcasters (in the Live-broadcast profile) in the channel exceeds 17.
         * @param uid ID of the user whose audio state changes.
         * @param state State of the remote audio
         * REMOTE_AUDIO_STATE_STOPPED(0): The remote audio is in the default state, probably due
         * to REMOTE_AUDIO_REASON_LOCAL_MUTED(3), REMOTE_AUDIO_REASON_REMOTE_MUTED(5),
         * or REMOTE_AUDIO_REASON_REMOTE_OFFLINE(7).
         * REMOTE_AUDIO_STATE_STARTING(1): The first remote audio packet is received.
         * REMOTE_AUDIO_STATE_DECODING(2): The remote audio stream is decoded and plays normally,
         * probably due to REMOTE_AUDIO_REASON_NETWORK_RECOVERY(2),
         * REMOTE_AUDIO_REASON_LOCAL_UNMUTED(4) or REMOTE_AUDIO_REASON_REMOTE_UNMUTED(6).
         * REMOTE_AUDIO_STATE_FROZEN(3): The remote audio is frozen, probably due to
         * REMOTE_AUDIO_REASON_NETWORK_CONGESTION(1).
         * REMOTE_AUDIO_STATE_FAILED(4): The remote audio fails to start, probably due to
         * REMOTE_AUDIO_REASON_INTERNAL(0).
         * @param reason The reason of the remote audio state change.
         * REMOTE_AUDIO_REASON_INTERNAL(0): Internal reasons.
         * REMOTE_AUDIO_REASON_NETWORK_CONGESTION(1): Network congestion.
         * REMOTE_AUDIO_REASON_NETWORK_RECOVERY(2): Network recovery.
         * REMOTE_AUDIO_REASON_LOCAL_MUTED(3): The local user stops receiving the remote audio
         * stream or disables the audio module.
         * REMOTE_AUDIO_REASON_LOCAL_UNMUTED(4): The local user resumes receiving the remote audio
         * stream or enables the audio module.
         * REMOTE_AUDIO_REASON_REMOTE_MUTED(5): The remote user stops sending the audio stream or
         * disables the audio module.
         * REMOTE_AUDIO_REASON_REMOTE_UNMUTED(6): The remote user resumes sending the audio stream
         * or enables the audio module.
         * REMOTE_AUDIO_REASON_REMOTE_OFFLINE(7): The remote user leaves the channel.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method
         * until the SDK triggers this callback.
         */
        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
            Log.i(
               TAG,
                "onRemoteAudioStateChanged->$uid, state->$state, reason->$reason"
            )
        }

        /**Since v2.9.0.
         * Occurs when the remote video state changes.
         * PS: This callback does not work properly when the number of users (in the Communication
         * profile) or broadcasters (in the Live-broadcast profile) in the channel exceeds 17.
         * @param uid ID of the remote user whose video state changes.
         * @param state State of the remote video:
         * REMOTE_VIDEO_STATE_STOPPED(0): The remote video is in the default state, probably due
         * to REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED(3), REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED(5),
         * or REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE(7).
         * REMOTE_VIDEO_STATE_STARTING(1): The first remote video packet is received.
         * REMOTE_VIDEO_STATE_DECODING(2): The remote video stream is decoded and plays normally,
         * probably due to REMOTE_VIDEO_STATE_REASON_NETWORK_RECOVERY (2),
         * REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED(4), REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED(6),
         * or REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK_RECOVERY(9).
         * REMOTE_VIDEO_STATE_FROZEN(3): The remote video is frozen, probably due to
         * REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION(1) or REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK(8).
         * REMOTE_VIDEO_STATE_FAILED(4): The remote video fails to start, probably due to
         * REMOTE_VIDEO_STATE_REASON_INTERNAL(0).
         * @param reason The reason of the remote video state change:
         * REMOTE_VIDEO_STATE_REASON_INTERNAL(0): Internal reasons.
         * REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION(1): Network congestion.
         * REMOTE_VIDEO_STATE_REASON_NETWORK_RECOVERY(2): Network recovery.
         * REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED(3): The local user stops receiving the remote
         * video stream or disables the video module.
         * REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED(4): The local user resumes receiving the remote
         * video stream or enables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED(5): The remote user stops sending the video
         * stream or disables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED(6): The remote user resumes sending the video
         * stream or enables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE(7): The remote user leaves the channel.
         * REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK(8): The remote media stream falls back to the
         * audio-only stream due to poor network conditions.
         * REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK_RECOVERY(9): The remote media stream switches
         * back to the video stream after the network conditions improve.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until
         * the SDK triggers this callback.
         */
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            Log.i(
               TAG,
                "onRemoteVideoStateChanged->$uid, state->$state, reason->$reason"
            )
        }

        /**Occurs when a remote user (Communication)/host (Live Broadcast) joins the channel.
         * @param uid ID of the user whose audio state changes.
         * @param elapsed Time delay (ms) from the local user calling joinChannel/setClientRole
         * until this callback is triggered.
         */
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(
               TAG,
                "onUserJoined->$uid"
            )
            showLongToast(String.format("user %d joined!", uid))
            /**Check if the context is correct */
            /**Check if the context is correct */
            handler?.post {
                /**Display remote video stream */
                var surfaceView: SurfaceView? = null
                if (fl_remote!!.childCount > 0) {
                    fl_remote!!.removeAllViews()
                }
                // Create render view by RtcEngine
                surfaceView = RtcEngine.CreateRendererView(this@MainActivity)
                surfaceView.setZOrderMediaOverlay(true)
                // Add to the remote container
                fl_remote!!.addView(
                    surfaceView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                // Setup remote video to render
                engine!!.setupRemoteVideo(
                    VideoCanvas(
                        surfaceView,
                        VideoCanvas.RENDER_MODE_HIDDEN,
                        uid
                    )
                )
            }
        }

        /**Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         * @param uid ID of the user whose audio state changes.
         * @param reason Reason why the user goes offline:
         * USER_OFFLINE_QUIT(0): The user left the current channel.
         * USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data
         * packet was received within a certain period of time. If a user quits the
         * call and the message is not passed to the SDK (due to an unreliable channel),
         * the SDK assumes the user dropped offline.
         * USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from
         * the host to the audience.
         */
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(
               TAG,
                String.format("user %d offline! reason:%d", uid, reason)
            )
            showLongToast(String.format("user %d offline! reason:%d", uid, reason))
            handler?.post(Runnable {
                /**Clear render view
                 * Note: The video will stay at its last frame, to completely remove it you will need to
                 * remove the SurfaceView from its parent */
                /**Clear render view
                 * Note: The video will stay at its last frame, to completely remove it you will need to
                 * remove the SurfaceView from its parent */
                /**Clear render view
                 * Note: The video will stay at its last frame, to completely remove it you will need to
                 * remove the SurfaceView from its parent */

                /**Clear render view
                 * Note: The video will stay at its last frame, to completely remove it you will need to
                 * remove the SurfaceView from its parent */
                engine!!.setupRemoteVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            })
        }
    }
    private fun showLongToast(msg: String?) {
        handler?.post {
            Toast.makeText(this.applicationContext, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /**leaveChannel and Destroy the RtcEngine instance*/
        /**leaveChannel and Destroy the RtcEngine instance */
        if (engine != null) {
            engine!!.leaveChannel()
        }
        handler!!.post { RtcEngine.destroy() }
        engine = null

        unregisterReceiver(receiver)
    }

    private fun isSerivceRunning():Boolean{
        val manager=getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(service in manager.getRunningServices(Int.MAX_VALUE)){
            if( FloatingWindowApp::class.java.name == service.service.className){
                return true
            }
        }
        return false
    }

    private fun requestFloatingWindowPermission(){
    val builder=AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Screen overlay Permission needed")
        builder.setMessage("Enable dispaly over the app from setting")
        builder.setPositiveButton("Open setting",DialogInterface.OnClickListener { dialog, which ->
            val intent=Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RESULT_OK)
        })

        dialog=builder.create()
        dialog.show()
    }

    private fun checkOverlayPermission():Boolean{
        return if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
                Settings.canDrawOverlays(this)
        }
        else  return true
    }


    private fun registerReceiver (){
        val filter = IntentFilter()
        filter.addAction("BROADCAST_ACTION")
        registerReceiver(receiver, filter)
    }

private fun getScreenCaptureParameters():ScreenCaptureParameters{
    val screenCaptureParameters = ScreenCaptureParameters()
    screenCaptureParameters.captureAudio = true
    screenCaptureParameters.captureVideo = true
    val videoCaptureParameters = ScreenCaptureParameters.VideoCaptureParameters()
    screenCaptureParameters.videoCaptureParameters = videoCaptureParameters
    return screenCaptureParameters
}
}