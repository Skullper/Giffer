package com.a2lab.project.giftest.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.a2lab.project.giftest.R
import com.a2lab.project.giftest.arch.BaseActivity
import com.a2lab.project.giftest.extensions.gone
import com.a2lab.project.giftest.extensions.showSnack
import com.a2lab.project.giftest.extensions.visible
import com.a2lab.project.giftest.main.presentation.MainPresenter
import com.a2lab.project.giftest.main.presentation.MainView
import com.a2lab.project.giftest.permissions.Demander
import com.a2lab.project.giftest.preview.PreviewActivity
import com.a2lab.project.giftest.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity


class MainActivity : BaseActivity<MainPresenter>(), MainView, View.OnClickListener {

    private val camera: CameraUtil by lazy { CameraUtil(this) }
    private val demander: Demander by lazy { Demander(this) }
    private var recordTime = 6F

    override fun providePresenter(): MainPresenter = MainPresenter(this)

    override fun getLayoutResource(): Int = R.layout.activity_main

    override fun bindViews() {
        presenter.getRecordTime {
            recordTime = it ?: 6F
        }
        askPermissions()
        mainAct_startIV.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (demander.isGranted)
            start()
        else askPermissions()
    }

    override fun showMessage(message: SimpleMessage) {
        showSnack(message)
    }

    private fun askPermissions() {
        demander.demand(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun start() {
        mainAct_timerTV.visible()
        mainAct_startIV.gone()
        presenter.startTimer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainAct_startIV.visible()
        mainAct_timerTV.gone()
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = Uri.parse(data?.dataString).path
                startActivity<PreviewActivity>(PATH_TO_VIDEO to uri)
            } else showMessage(Resource(R.string.mainAct_notFoundResultException))
        } else showMessage(Resource(R.string.mainAct_notFoundRequestException))
    }

    override fun onTimerFinished() = camera.start(recordTime)

    override fun updateTimerView(seconds: String) {
        mainAct_timerTV.text = seconds
    }

}
