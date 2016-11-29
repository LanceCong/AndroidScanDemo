package deyuan.cn.cnicg.saodemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import deyuan.cn.cnicg.saodemo.zxing.CameraManager;
import deyuan.cn.cnicg.saodemo.zxing.CaptureActivityHandler;
import deyuan.cn.cnicg.saodemo.zxing.InactivityTimer;
import deyuan.cn.cnicg.saodemo.zxing.ViewfinderView;

public class CaptureActivity extends Activity implements Callback,
		OnClickListener {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.90f;
	private boolean vibrate;

	private TextView capture_back;
	private TextView capture_more;
 
	private Camera m_Camera = null;
	private Parameters parameters = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera);

		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

		capture_back = (TextView) findViewById(R.id.activity_sao_back);
		capture_back.setOnClickListener(this);
		capture_more = (TextView) findViewById(R.id.activity_sao_more);
		capture_more.setOnClickListener(this);

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// ToastUtil.toastshow(CaptureActivity.this, "执行了了");
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {

			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}

		vibrate = true;

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * Handler scan result
	 * 
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();

		final String resultString = result.getText();

		Toast.makeText(CaptureActivity.this, resultString,Toast.LENGTH_SHORT).show();

		Intent intent = new Intent();
		intent.putExtra("result",resultString);
		setResult(0,intent);
		finish();

	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	boolean open = false;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_sao_back:
			this.finish();
			break;

		case R.id.activity_sao_more:
			CameraManager.get().handleLight(!open);
			open = !open;
			break;

		default:
			break;
		}
	}

	private void OpenLightOn() {
		if (null == m_Camera) {
			m_Camera = Camera.open();
		}

		Parameters parameters = m_Camera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		m_Camera.setParameters(parameters);
		m_Camera.autoFocus(new Camera.AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
			}
		});
		m_Camera.startPreview();
		open = true;
	}

	private void CloseLightOff() {

		if (m_Camera != null) {
			m_Camera.stopPreview();
			m_Camera.release();
			m_Camera = null;
		}
		open = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			this.finish();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}