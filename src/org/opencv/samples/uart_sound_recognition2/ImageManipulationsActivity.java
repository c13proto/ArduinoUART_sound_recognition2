package org.opencv.samples.uart_sound_recognition2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2,SensorEventListener {
    private static final String  TAG                 = "OCVSample::Activity";
    static final int      VIEW_MODE_RGBA      = 0;
    static final int      VIEW_MODE_HIST      = 1;
    static final int      VIEW_MODE_CANNY     = 2;
    static final int      VIEW_MODE_SEPIA     = 3;
    static final int      VIEW_MODE_SOBEL     = 4;
    static final int      VIEW_MODE_ZOOM      = 5;
    static final int      VIEW_MODE_PIXELIZE  = 6;
    static final int      VIEW_MODE_POSTERIZE = 7;
    static final int		 VIEW_MODE_PANEL	 = 8;
    static final int		 VIEW_MODE_DITECT	 = 9;
    static final int		 VIEW_MODE_RECOG	 = 10;
    
    static int           viewMode = VIEW_MODE_RGBA;
    
    static int		 	split_pixel		 	 =4;
    static final int 	 frame_cols			 = 960;//フレーム幅
    static final int 	 frame_rows			 = 720;//フレーム高さ
    
    static SoundPool sound_effect;//サウンドエフェクトのまとまり
    static int a2m_id,chase_finish_id,m2a_id,command_changed_id;//各音声ファイルのid登録
    
    private MenuItem			 mItemPreviewPanel;
    private MenuItem			 mItemPreviewDitect;
    //private MenuItem			 mItemSoundRecognizer;
    
    private MenuItem             mItemPreviewRGBA;
    private MenuItem             mItemPreviewHist;
    private MenuItem             mItemPreviewCanny;
    private MenuItem             mItemPreviewSepia;
    private MenuItem             mItemPreviewSobel;
    private MenuItem             mItemPreviewZoom;
    private MenuItem             mItemPreviewPixelize;
    private MenuItem             mItemPreviewPosterize;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Size                 mSize0;

    private Mat                  mIntermediateMat;
    private Mat                  mMat0;
    private MatOfInt             mChannels[];
    private MatOfInt             mHistSize;
    private int                  mHistSizeNum = 25;
    private MatOfFloat           mRanges;
    private Scalar               mColorsRGB[];
    private Scalar               mColorsHue[];
    private Scalar               mWhilte;
    private Point                mP1;
    private Point                mP2;
    private float                mBuff[];
    private Mat                  mSepiaKernel;

    //音声認識
    static  boolean RecEnable=false;
    static SpeechRecognizer mSpeechRecognizer;
    
    View Panel;
    View Camera;
    boolean mIsPanel=true;
	private SensorManager mSensorManager;
    private static final int MATRIX_SIZE = 16;
    Timer timer;
    /* 回転行列 */
    float[]  inR = new float[MATRIX_SIZE];
    float[] outR = new float[MATRIX_SIZE];
    float[]    I = new float[MATRIX_SIZE];
 
    /* センサーの値 */
    static float[] orientationValues   = new float[3];//傾き
    static float[] accelerometerValues = new float[3];//加速度
    static float[] magneticValues      = new float[3];//地磁気
    //static float[] gyroscopeValues	   = new float[3];//ジャイロ
    
	
    static boolean mIsActivity=true;
    static boolean mIsMagSensor;
    static boolean mIsAccSensor;
    static boolean mIsGyroSensor;
    static boolean mIsJoypad;
    
    UsbSerialDriver usb;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ImageManipulationsActivity() {
        //Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

	    
	    //Log.i(TAG, "called onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.image_manipulations_surface_view);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //Camera=this.getLayoutInflater().inflate(R.layout.image_manipulations_surface_view,null);
        
        
        //パネルのビュー
        Panel=this.getLayoutInflater().inflate(R.layout.main,null);
        addContentView(Panel, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        Panel.setVisibility(View.VISIBLE);
	    /* センサ・マネージャを取得する */
	    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    /* USBマネージャを取得する */
	    UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
	    	usb = UsbSerialProber.acquire(manager);
	    if (usb != null) {
	      try{
	        usb.open();
	        usb.setBaudRate(115200);
	        uart.start_read_and_write_thread(usb); // シリアル通信を読むスレッドを起動
	      }
	      catch(IOException e){
	        e.printStackTrace();
	      }
	    }
	    
	    //音声認識
	    mSpeechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
	    mSpeechRecognizer.setRecognitionListener(new mRecognitionListner());
	    
    }

    @Override
    public void onResume()
    {
        super.onResume();
                
        mIsActivity=true;
        //camera
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
        // センサの取得
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
 
        // センサマネージャへリスナーを登録(implements SensorEventListenerにより、thisで登録する)
        for (Sensor sensor : sensors) {
            if( sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
                mIsMagSensor = true;
                Log.v("TYPE_MAGNETIC_FIELD","get_ID");
            }
 
            if( sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
                mIsAccSensor = true;
                Log.v("TYPE_ACCELEROMETER","get_ID");
            }
            /*
            if(sensor.getType()==Sensor.TYPE_GYROSCOPE){
            	mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            	mIsGyroSensor=true;
            	Log.v("TYPE_GYROSCOPE","get_ID");
            }
            */
            //SENSOR_DELAY_FASTEST 最高速でのセンサ読み出し
            //SENSOR_DELAY_GAME	 高速ゲーム向け
            //SENSOR_DELAY_NORMAL 通常モード
            //SENSOR_DELAY_UI 低速。ユーザインターフェイス向け
        }
        
	    //効果音の準備
	    sound_effect=new SoundPool(3,AudioManager.STREAM_MUSIC,0);
	    a2m_id = sound_effect.load(this, R.raw.a2m, 1);
	    m2a_id = sound_effect.load(this, R.raw.m2a, 1);
	    chase_finish_id = sound_effect.load(this, R.raw.chase_finish, 1);
	    command_changed_id= sound_effect.load(this, R.raw.command_changed,1);
	    
        
        startTimer(this);//情報を40msおきに表示させる
        
    }

    @Override
    public void onPause()
    {

    	super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        
        
        mIsActivity=false;
        //センサーマネージャのリスナ登録破棄
        if (mIsMagSensor || mIsAccSensor||mIsGyroSensor) 
        {
            mSensorManager.unregisterListener(this);
            
            mIsMagSensor = false;
            mIsAccSensor = false;
        	mIsGyroSensor= false;
        }
        timer.cancel();//タイマー終了
        sound_effect.release();//サウンドエフェクト終了
        finish();//タスク終了
        //Log.d("onPause","onPause");

    }
    
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();

    }
    

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
    public void onSensorChanged(SensorEvent event) {
     	if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
  
	     switch (event.sensor.getType()) {
	         case Sensor.TYPE_MAGNETIC_FIELD:
	        	 //Log.v("TYPE_MAGNETIC_FIELD","get_value");
	             magneticValues = event.values.clone();
	             break;
	         case Sensor.TYPE_ACCELEROMETER:
	        	 //Log.v("TYPE_ACCELEROMETER","get_value");
	             accelerometerValues = event.values.clone();
	             break;
	             
	             /*
	         case Sensor.TYPE_GYROSCOPE:
	        	 //Log.v("TYPE_GYROSCOPE","get_value");
	        	 gyroscopeValues=event.values.clone();
	        	 break;
	        	 */
	     }
	  
	     if (magneticValues != null && accelerometerValues != null) {//地磁気と加速度から傾きの取得
	  
	         SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues);
	  
	         //Activityの表示が縦固定の場合。横向きになる場合、修正が必要です
	         SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
	         SensorManager.getOrientation(outR, orientationValues);//地磁気と加速度から傾きを求める	         
	         machine_ctrl.get_absolute_degree();//傾きからマシンの絶対角度計算
	     }
    }
	@Override
	public boolean dispatchKeyEvent(KeyEvent ev) {//キーイベント、コントローラで終了させない。PSボタン制御不可
		int joypad_count=0;
		controller.key_operation(ev);
		for(int i=0;i<17;i++)if(controller.DualShock3[i])joypad_count++;
		if(joypad_count>0)return true;
		else return super.dispatchKeyEvent(ev);
	}
	
	@Override
	 public boolean dispatchGenericMotionEvent(MotionEvent event) {
		 // Check that the event came from a joystick since a generic motion event
		 // could be almost anything.
		if(event.getSource()==InputDevice.SOURCE_JOYSTICK||event.getSource()==InputDevice.SOURCE_GAMEPAD)
		{
			controller.joystick_operation(event);
			mIsJoypad=true;
		}
		else mIsJoypad=false;
		 return super.dispatchGenericMotionEvent(event);
	 }
	
    public void startTimer(final ImageManipulationsActivity mv){
    	
    	if(timer!=null)timer.cancel();
    	timer=new Timer();
    	final android.os.Handler handler=new android.os.Handler();
    	timer.schedule(new TimerTask(){
    		int timecount=0;//10msおきに数える

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						if(timecount==40000)timecount=0;
						else timecount++;
						
						machine_ctrl.get_angle_velocity();//角速度取得
						if(timecount%4==0){
							show.show_debug(mv);//デバッグ情報を40msおきに表示
							show.show_arduino(mv);
							show.show_android(mv);
							show.show_center_hsv(mv);
							if(RecEnable)mRecognitionListner.voice_command_operation(mv);//音声認識コマンド
						}
					}
				});
			}
    	},0,10);//10ms
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewPanel=menu.add("パネル");
        mItemPreviewDitect=menu.add("色検出");
        //mItemSoundRecognizer=menu.add("音認識");
        mItemPreviewRGBA  = menu.add("RGBA");
        mItemPreviewHist  = menu.add("ヒストグラム");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewSepia = menu.add("Sepia");
        mItemPreviewSobel = menu.add("Sobel");
        mItemPreviewZoom  = menu.add("Zoom");
        mItemPreviewPixelize  = menu.add("Pixelize");
        mItemPreviewPosterize = menu.add("Posterize");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        
        if (item == mItemPreviewRGBA)		viewMode = VIEW_MODE_RGBA;
        if (item == mItemPreviewHist)		viewMode = VIEW_MODE_HIST;
        else if (item == mItemPreviewCanny)	viewMode = VIEW_MODE_CANNY;
        else if (item == mItemPreviewSepia)	viewMode = VIEW_MODE_SEPIA;
        else if (item == mItemPreviewSobel)	viewMode = VIEW_MODE_SOBEL;
        else if (item == mItemPreviewZoom)	viewMode = VIEW_MODE_ZOOM;
        else if (item == mItemPreviewPixelize)viewMode = VIEW_MODE_PIXELIZE;
        else if (item == mItemPreviewPosterize)viewMode = VIEW_MODE_POSTERIZE;
        else if (item == mItemPreviewDitect)viewMode = VIEW_MODE_DITECT;
        else if (item == mItemPreviewPanel){
        	if(!mIsPanel){
	        	mIsPanel=true;
	        	Panel.setVisibility(View.VISIBLE);
	        	//Camera.setVisibility(View.INVISIBLE);意味ない
        	}
        	else{
        		mIsPanel=false;
        		Panel.setVisibility(View.INVISIBLE);
        		//Camera.setVisibility(View.VISIBLE);
        	}
        }
//    	else if(item==mItemSoundRecognizer){//音声認識
//    		
//    		viewMode = VIEW_MODE_RECOG;
//    		RecEnable=!RecEnable;//音声認識を有効無効をトグル式に切り替える
//    		mRecognitionListner.startVR();
//
//    	}
        
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0  = new Mat();
        mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        mColorsHue = new Scalar[] {
                new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        
        if(color_ditection.check( ((EditText)findViewById(R.id.split_pixel)).getText().toString())){
        	split_pixel=Integer.parseInt( ((EditText)findViewById(R.id.split_pixel) ).getText().toString() );
        }
        else split_pixel=1;
        
        //リサイズ。うまくいかない
        Imgproc.resize(rgba, rgba, new Size(), 1.0/split_pixel,1.0/split_pixel, Imgproc.INTER_LINEAR);

        Size sizeRgba = rgba.size();
        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;
        
        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;
        
        color_ditection.get_center_hsv(rgba);//中央のhsv値取得
        
        switch (ImageManipulationsActivity.viewMode) {
        case ImageManipulationsActivity.VIEW_MODE_RGBA:
            break;

        case ImageManipulationsActivity.VIEW_MODE_HIST:
            Mat hist = new Mat();
            int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
            if(thikness > 5) thikness = 5;
            int offset = (int) ((sizeRgba.width - (5*mHistSizeNum + 4*10)*thikness)/2);
            // RGB
            for(int c=0; c<3; c++) {
                Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for(int h=0; h<mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height-1;
                    mP2.y = mP1.y - 2 - (int)mBuff[h];
                    Core.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
                }
            }
            // Value and Hue
            Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
            // Value
            Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - 2 - (int)mBuff[h];
                Core.line(rgba, mP1, mP2, mWhilte, thikness);
            }
            // Hue
            Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - 2 - (int)mBuff[h];
                Core.line(rgba, mP1, mP2, mColorsHue[h], thikness);
            }
            break;

        case ImageManipulationsActivity.VIEW_MODE_CANNY:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            rgbaInnerWindow.release();
            break;

        case ImageManipulationsActivity.VIEW_MODE_SOBEL:
            Mat gray = inputFrame.gray();
            Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
            Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            grayInnerWindow.release();
            rgbaInnerWindow.release();
            break;

        case ImageManipulationsActivity.VIEW_MODE_SEPIA:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
            rgbaInnerWindow.release();
            break;

        case ImageManipulationsActivity.VIEW_MODE_ZOOM:
            Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0, cols / 2 - cols / 10);
            Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
            Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
            Size wsize = mZoomWindow.size();
            Core.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
            zoomCorner.release();
            mZoomWindow.release();
            break;

        case ImageManipulationsActivity.VIEW_MODE_PIXELIZE:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.resize(rgbaInnerWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
            Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
            rgbaInnerWindow.release();
            break;

        case ImageManipulationsActivity.VIEW_MODE_POSTERIZE:
            /*
            Imgproc.cvtColor(rgbaInnerWindow, mIntermediateMat, Imgproc.COLOR_RGBA2RGB);
            Imgproc.pyrMeanShiftFiltering(mIntermediateMat, mIntermediateMat, 5, 50);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_RGB2RGBA);
            */
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
            rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
            Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1./16, 0);
            Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
            rgbaInnerWindow.release();
            break;
        case ImageManipulationsActivity.VIEW_MODE_DITECT:
        	rgba=color_ditection.ditect_colors(this,rgba,5);
        	break;
        }
        
        Imgproc.resize(rgba, rgba, new Size(), split_pixel,split_pixel, Imgproc.INTER_LINEAR);//画像処理後サイズを戻す
        return rgba;
    }
    

}