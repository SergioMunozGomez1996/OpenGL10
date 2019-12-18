package com.japg.mastermoviles.opengl10;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class OpenGLActivity extends AppCompatActivity {
	private GLSurfaceView glSurfaceView;
	private boolean rendererSet = false;

	// Detector de gestos.
	private ScaleGestureDetector scaleGestureDetector;
	// Factor de escalado, inicialmente a 0.
	private float scaleFactor = 0.0f;

	// Punto activo de la pantalla al presionarla.
	private int activePointerID = -1;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_open_gl1);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_gl2);
		setContentView(R.layout.activity_open_gl2);

		glSurfaceView = findViewById(R.id.glSurfaceView);
		final OpenGLRenderer openGLRenderer = new OpenGLRenderer(this);
		final ActivityManager activityManager =
				(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo =
				activityManager.getDeviceConfigurationInfo();
		//final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
		final boolean supportsEs2 =
				configurationInfo.reqGlEsVersion >= 0x20000
						|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
						&& (Build.FINGERPRINT.startsWith("generic")
						|| Build.FINGERPRINT.startsWith("unknown")
						|| Build.MODEL.contains("google_sdk")
						|| Build.MODEL.contains("Emulator")
						|| Build.MODEL.contains("Android SDK built for x86")));
		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			glSurfaceView.setEGLContextClientVersion(2);
			// Para que funcione en el emulador
			glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			// Asigna nuestro renderer.
			glSurfaceView.setRenderer(openGLRenderer);
			rendererSet = true;
			Toast.makeText(this, "OpenGL ES 2.0 soportado",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Este dispositivo no soporta OpenGL ES 2.0",
					Toast.LENGTH_LONG).show();
			return;
		}

		// Inicializamos el detector de gestos y le asignamos un listener para detectar el evento de pinza.
		scaleGestureDetector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener());
		// Obtenemos el factor de escalado actual.
		scaleFactor = openGLRenderer.getScale();

		glSurfaceView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event != null) {
					// Si se detecta un evento de toque lo vinculamos con el detector de gestos declarado.
					scaleGestureDetector.onTouchEvent(event);
					// Convert touch coordinates into normalized device
					// coordinates, keeping in mind that Android's Y
					// coordinates are inverted.
					final float normalizedX =   (event.getX() / (float) v.getWidth()) * 2 - 1;
					final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// Obtenemos el ID del punto tocado inicialmente al pulsar con el botón.
						activePointerID = event.getPointerId(0);
						glSurfaceView.queueEvent(new Runnable() {
							@Override
							public void run() {
								openGLRenderer.handleTouchPress(normalizedX, normalizedY);
							}
						});
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						// Obtenemos el punto inicialmente tocado al comenzar el movimiento.
						int currentPointerID = event.findPointerIndex(activePointerID);
						// Si no hay un detector de gestos actualmente en progreso, solo se ha detectado un toque simultáneo y los puntos
						// coinciden, se trata de un movimiento de rotación.
						if (!scaleGestureDetector.isInProgress() && event.getPointerCount() == 1 && currentPointerID == activePointerID) {
							glSurfaceView.queueEvent(new Runnable() {
								@Override
								public void run() {
									openGLRenderer.handleTouchDrag(normalizedX, normalizedY);
								}
							});
						} else {
							// Si no, será un movimiento "pinza".

							// En el método onScale del listener se recalculará el factor de escalado. Llamaremos al método para escalar la figura.
							openGLRenderer.setScale(scaleFactor);
						}
					}
					return true;
				} else {
					return false;
				}
			}
		});

		SeekBar seekBar = findViewById(R.id.seekbar);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				openGLRenderer.handleSeekBar(i);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (rendererSet) {
			glSurfaceView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (rendererSet) {
			glSurfaceView.onResume();
		}
	}


	// Listener para detectar gestos de escalado.
	private class SimpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor /= detector.getScaleFactor();
			// Puede limitarse el factor de escalado a la hora de recalcularlo.
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, OpenGLRenderer.DEFAULT_ZOOM));
			Log.e("APP", Float.toString(scaleFactor));
			return true;
		}
	}

}
