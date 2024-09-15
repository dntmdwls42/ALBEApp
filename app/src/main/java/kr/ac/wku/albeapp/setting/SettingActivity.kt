// SettingActivity.kt
package kr.ac.wku.albeapp.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kr.ac.wku.albeapp.databinding.ActivitySettingBinding
import kr.ac.wku.albeapp.logins.LoginPageActivity

/**
 * MVVM 형식으로 변경된 환경설정 액티비티
 */
class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val viewModel: SettingViewModel by viewModels()

    private lateinit var phoneNumber: String
    private var isFromMainActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel과 바인딩 연결
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // 전화번호와 플래그를 가져옴
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        isFromMainActivity = intent.getBooleanExtra("isFromMainActivity", false)

        viewModel.initData(phoneNumber, isFromMainActivity)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isUserLoggedIn.observe(this, Observer { isLoggedIn ->
            binding.userdelete.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        })

        viewModel.isUserDeleteVisible.observe(this, Observer { isVisible ->
            binding.userdelete.visibility = if (isVisible) View.VISIBLE else View.GONE
        })

        viewModel.seekBarProgress.observe(this, Observer { progress ->
            binding.sensorsetting.progress = progress
        })

        viewModel.sensorOff.observe(this, Observer { isOff ->
            binding.sensoroff.isChecked = isOff
        })

        viewModel.toastMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        viewModel.backButtonEvent.observe(this, Observer {
            finish()
        })

        viewModel.showDeleteAccountDialogEvent.observe(this, Observer {
            showDeleteAccountDialog()
        })
    }

    private fun setupListeners() {
        binding.logout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }

        binding.moresetting.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleUserDeleteVisibility(isChecked)
        }

        binding.userdelete.setOnClickListener {
            showDeleteAccountDialog()
        }

        binding.backgroundoff.setOnClickListener {
            viewModel.stopServices()
        }

        binding.backgroundon.setOnClickListener {
            viewModel.startServices()
        }

        binding.sensoroff.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSensorUsage(isChecked)
        }

        binding.sensorsetting.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                viewModel.updateSensorInterval(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.allnotifyoff.setOnClickListener {
            viewModel.openNotificationSettings()
        }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말 탈퇴하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                viewModel.deleteAccount()
                startActivity(Intent(this, LoginPageActivity::class.java))
                finish()
            }
            .setNegativeButton("아니오", null)
            .show()
    }
}
