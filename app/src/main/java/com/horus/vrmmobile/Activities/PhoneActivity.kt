package com.horus.vrmmobile.Activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.EventBus
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import android.view.animation.AnimationUtils
import android.os.CountDownTimer
import com.google.firebase.auth.PhoneAuthProvider
import com.androidnetworking.error.ANError
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.AndroidNetworking
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.androidnetworking.common.Priority
import com.auth0.android.jwt.JWT
import com.goodiebag.pinview.Pinview
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Person
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PersonRepository
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.events.EventFcmToken
import com.horus.vrmmobile.services.DownloadService
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.content_verify_code.*
import kotlinx.android.synthetic.main.phone_activity.*
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {
    private var mVerificationInProgress = false
    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mAuth: FirebaseAuth? = null
    private var phone: String? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_activity)
        window.setBackgroundDrawableResource(R.drawable.bg_gradient_white)
        loadingProgress.visibility = View.INVISIBLE

        setEvents()

        showView(verifyLayout)
        hideView(inputCodeLayout)
        hideView(loadingProgress)

        loginButton.setOnClickListener { attemptLogin() }
        resend_code.setOnClickListener { retryVerify() }
        reenter_phone_number.setOnClickListener { closeSession() }


        mAuth = FirebaseAuth.getInstance()
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                } else if (e is FirebaseTooManyRequestsException) {
                }
            }

            override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                Log.d(TAG, "onCodeSent:" + verificationId!!)
                mVerificationId = verificationId
                SharedConfig.setPhoneVerificationId(verificationId)
                mResendToken = token
            }
        }
        sms_code!!.setPinViewEventListener(Pinview.PinViewEventListener { pinview, b ->
            val verifyCode = sms_code!!.getValue()
            verifyPhoneNumberWithCode(SharedConfig.getPhoneVerificationId(), verifyCode)
        })

        if(!SharedConfig.getToken().isEmpty() && !SharedConfig.getPhoneVerificationId().isEmpty()){
            hideView(verifyLayout)
            showView(inputCodeLayout)
            hideView(loadingProgress)
            hideView(timer_layout)
            phone = SharedConfig.getPhoneLogin()
        }
    }

    private fun closeSession() {
        SharedConfig.clear()
        val i = Intent(this, PhoneActivity::class.java)
        startActivity(i)
        finish()
    }


    private fun setEvents(){
        phone_code.inputType = InputType.TYPE_CLASS_NUMBER
        phone_code.keyListener = DigitsKeyListener.getInstance("1234567890")
        val listener = MaskedTextChangedListener.installOn(
                phone_code,
                "[9900]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    }
                }
        )
        phone_code.addTextChangedListener(listener)

        phone_number.inputType = InputType.TYPE_CLASS_NUMBER
        phone_number.keyListener = DigitsKeyListener.getInstance("1234567890")
        val listener2 = MaskedTextChangedListener.installOn(
                phone_number,
                "15[99999990]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    }
                }
        )
        phone_number.addTextChangedListener(listener2)
        phone_number.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = v!!.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    attemptLogin()
                    return true
                }
                return false
            }
        })

        phone_code.requestFocus()
    }

    private fun retryVerify() {
        if (mResendToken == null) {
            startPhoneNumberVerification(phone!!)
        } else {
            resendVerificationCode(phone, mResendToken)
        }
    }


    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        hideView(verifyLayout!!)
        hideView(inputCodeLayout!!)
        showView(loadingProgress!!)

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(phoneNumber: String?,token: PhoneAuthProvider.ForceResendingToken?) {
        initContador()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber!!,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks!!,
                token)
    }


    private fun attemptLogin() {
        phone_number.error = null
        phone_code.error = null
        if(phone_code.text.toString().equals("")){
            var a = Utils.getSweetAlert(this@PhoneActivity,1,"Notificación","Ingrese código de área.")
            a.show()
            return
        }
        if(phone_number.text.toString().equals("15")){
            var b = Utils.getSweetAlert(this@PhoneActivity,1,"Notificación","Ingrese número de teléfono.")
            b.show()
            return
        }
        val txt = AppCompatEditText(this@PhoneActivity)
        txt.setText("9" + phone_code.text.toString().replaceFirst("^0+(?!$)".toRegex(), "") + phone_number.text.toString().replaceFirst("^15{1}(?!$)".toRegex(), ""))
        ccp.registerPhoneNumberTextView(txt)
        phone = ccp.fullNumber
        phone = "+$phone"

        var cancel = false
        var focusView: View? = null

        if (!isPhoneValid(phone!!)) {
            focusView = phone_number
            cancel = true
        }
        if (cancel) {
            focusView!!.requestFocus()
        } else {
            hideView(verifyLayout!!)
            hideView(inputCodeLayout!!)
            showView(loadingProgress!!)
            SharedConfig.setPhoneLogin(phone!!)
            searchUserInServer(phone!!)
        }
    }

    private fun sendRegistrationFCMToken(fcmToken:String){
        EventFcmToken.instance.sendRegistrationFCMToken(fcmToken)
    }
    private fun searchUserInServer(phone: String) {
        val url = Constant.urlVRMServer + "api/auth/token"
        AndroidNetworking.post(url)
                .addHeaders("Content-Type", "application/x-www-form-urlencoded")
                .addBodyParameter("grant_type", "password")
                .addBodyParameter("UserName", phone.replace("+", ""))
                .addBodyParameter("Password", Constant.secretKey)
                .setTag("auth")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {

                    override fun onResponse(response: JSONObject) {
                        try {

                            if (response.has("access_token")) {
                                SharedConfig.setToken(response.getString("access_token"))
                                SharedConfig.setUserId(response.getString("Id"))
                                SharedConfig.setUserName(response.getString("FirstName"))
                                SharedConfig.setUserSurname(response.getString("FirstSurname"))
                                SharedConfig.setZonePoliticalFrontId(response.getString("ZonePoliticalFrontId"))
                                SharedConfig.setPersonHierarchicalStructureId(response.getString("PersonHierarchicalStructureId"))
                                SharedConfig.setUserPhoto(response.getString("Photo"))
//                                SharedConfig.setPoliticalFrontId(response.getString("PoliticalFrontId"))

                                // set permissions
                                try {
                                    val jwt = JWT(SharedConfig.getToken())
                                    val jsonArray = JSONObject(jwt.claims["Role"]?.asString()).getJSONArray("Permissions")
                                    val permissions = HashSet<String>()
                                    for (i in 0 until jsonArray.length()) {
                                        permissions.add(jsonArray.getString(i))
                                    }
                                    SharedConfig.setPermissions(permissions)
                                }catch (e: Exception) {

                                }

                                // create person for user and save in realm db
                                val person = ModelSyncBuilder.create(Person())

                                person.Id = response.getString("Id")
                                person.FirstSurname = response.getString("FirstSurname")
                                person.SecondSurname = response.getString("SecondSurname")
                                person.FirstName = response.getString("FirstName")
                                person.SecondName = response.getString("SecondName")
//                                person.PhoneNumber = response.getString("PhoneNumber")
                                person.DocumentationNumber = response.getString("DocumentationNumber")
                                person.BirthDate = response.getString("BirthDate")
                                person.IdentificationTypeId = response.getString("IdentificationTypeId")
                                person.PersonTypeId = response.getString("PersonTypeId")
                                person.MaritialStatusId = response.getString("MaritialStatusId")
                                person.ReligionId = response.getString("ReligionId")
                                person.SexId = response.getString("SexId")
                                person.GenderId = response.getString("GenderId")
                                person.NationalityId = response.getString("NationalityId")

                                PersonRepository.instance.addOrUpdate(person, false)

                                sendRegistrationFCMToken(SharedConfig.getFcmToken())
                                hideView(verifyLayout)
                                showView(inputCodeLayout)
                                hideView(loadingProgress)
                                startPhoneNumberVerification(phone)
                            } else {
                                showView(verifyLayout)
                                hideView(inputCodeLayout)
                                hideView(loadingProgress)
                                Utils.showMaterialDialog(
                                        this@PhoneActivity,
                                        this@PhoneActivity.resources.getString(R.string.title),
                                        response.getString("error_description"),
                                        this@PhoneActivity.resources.getString(R.string.accept))
                            }
                        } catch (e: Exception) {
                            Utils.showMaterialDialog(
                                    this@PhoneActivity,
                                    this@PhoneActivity.resources.getString(R.string.title),
                                    "Error Api " + e.message,
                                    this@PhoneActivity.resources.getString(R.string.agree))
                            showView(verifyLayout)
                            hideView(inputCodeLayout)
                            hideView(loadingProgress)
                        }
                        Log.i(TAG, response.toString())
                    }

                    override fun onError(anError: ANError) {
                        var messageError = "Hubo un problema al conectarse con el servidor."
                        if(!anError.errorBody.isNullOrEmpty()){
                            try {
                                val errorJson = JSONObject(anError.errorBody)
                                messageError = errorJson.getString("error_description")
                            }catch (e: Exception){
                                Log.e("searchUserInServer", e.message)
                            }
                        }

                        Log.e("updateVersionData", messageError)
                        Utils.showMaterialDialog(
                                this@PhoneActivity,
                                this@PhoneActivity.resources.getString(R.string.title),
                                messageError,
                                this@PhoneActivity.resources.getString(R.string.agree))
                        showView(verifyLayout!!)
                        hideView(inputCodeLayout!!)
                        hideView(loadingProgress!!)
                    }
                })
    }

    private fun isPhoneValid(phone: String): Boolean {
        return true
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        initContador()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks!!)
        mVerificationInProgress = true
    }

    private fun initContador() {
        showView(timer_layout)
        if (countDownTimer != null) {
            try {
                countDownTimer!!.cancel()
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }

        }
        countDownTimer = object : CountDownTimer(45000, 1000) {
            override fun onTick(l: Long) {
                timer!!.text = "0:" + l / 1000 + " s"
                resend_code.visibility = View.INVISIBLE
                lbl_info.visibility = View.INVISIBLE
                reenter_phone_number.visibility = View.INVISIBLE
            }

            override fun onFinish() {
                timer!!.text = 0.toString() + " s"
                resend_code.startAnimation(AnimationUtils.loadAnimation(this@PhoneActivity, R.anim.abc_slide_in_bottom))
                resend_code.visibility = View.VISIBLE
                lbl_info.startAnimation(AnimationUtils.loadAnimation(this@PhoneActivity, R.anim.abc_slide_in_bottom))
                lbl_info.visibility = View.VISIBLE
                reenter_phone_number.startAnimation(AnimationUtils.loadAnimation(this@PhoneActivity, R.anim.abc_slide_in_bottom))
                reenter_phone_number.visibility = View.INVISIBLE

            }
        }
        countDownTimer!!.start()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")

                        runOnUiThread {
                            hideView(verifyLayout!!)
                            hideView(inputCodeLayout!!)
                            showView(loadingProgress!!)
                        }

                        // set verification success
                        SharedConfig.setPhoneVerified(true)

                        object : Thread() {
                            override fun run() {
//                                SyncService.pull()
                                EventBus.getDefault().post(Event("syncEnd"))
                            }
                        }.start()
                    } else {
                        SharedConfig.setToken("")
                        SharedConfig.setUserId("")
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this@PhoneActivity, R.string.cod_invalid, Toast.LENGTH_LONG).show()
                        }

                    }
                }
    }

    override fun onBackPressed() {
        if (inputCodeLayout!!.visibility == View.VISIBLE) {
            showView(verifyLayout!!)
            hideView(inputCodeLayout!!)
            hideView(loadingProgress!!)
            if (countDownTimer != null) {
                try {
                    countDownTimer!!.cancel()
                } catch (e: Exception) {
                    Log.e(TAG, e.message)
                }

            }
        } else {
            super.onBackPressed()
        }
    }

    private fun goMenu() {
        SharedConfig.setVerifyNumber(true)
        checkPermissions()
    }

    private fun showView(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE

        }
    }

    private fun hideView(vararg views: View) {
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    public override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    public override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventListener(v: Event) {
        when (v.eventName) {
            "syncEnd" -> goMenu()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val i = Intent(this, PermissionRequestActivity::class.java)
            startActivity(i)
            finish()
        } else {
            if (SharedConfig.isFirstDownloadFinish()) {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            } else {
                DownloadService.downloadCatalogs(this@PhoneActivity) {
                    val i = Intent(this@PhoneActivity, MainActivity::class.java)
                    i.putExtra("origen", "login")
                    startActivity(i)
                    finish()
                }
            }
        }
    }

    companion object {
        private val TAG = "PhoneAuthActivity"
    }
}
