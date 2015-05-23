package org.opencv.samples.uart_sound_recognition2;


import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CheckBox;
import java.text.DecimalFormat;

public class show {
	static String orientation="";
	static String acceleration="";
	static String ditect_info="";
	static String magnetic="";
	static String dualshock3="";
	static String debug="";
	static String arduino="";
	static String android="";
	static String hsv="";
	static String voice="";
	static String seekbar="";
	static DecimalFormat df1=new DecimalFormat("##0.00");
	static DecimalFormat df2=new DecimalFormat("##0.0000");
	
	private static boolean old_start=false;
	
	static void show_debug(ImageManipulationsActivity mv){

		orientation(mv);//傾き
		acceleration(mv);//加速度
	    color_info(mv);//色追跡
		magnetic(mv);//地磁気
		dualshock3(mv);//joypad
		voice(mv);//音声認識
		seekbar(mv);
		
		
		debug=orientation+acceleration+ditect_info+magnetic+dualshock3+machine_ctrl.ctrl_debug+voice+seekbar;
		((TextView)mv.findViewById(R.id.debug)).setText(debug);
	}
	static void show_center_hsv(ImageManipulationsActivity mv){//中央のHSV値表示
		hsv="["+(int)color_ditection.center_HSV[0]+","+
						   (int)color_ditection.center_HSV[1]+","+
						   (int)color_ditection.center_HSV[2]+"]";
		((TextView)mv.findViewById(R.id.hsv)).setText(hsv);
	}
	static void show_arduino(ImageManipulationsActivity mv){
		arduino="アルディーノ！"+"\n"+uart.arduino+uart.arduino_recieve_average+uart.android_recieve_average+uart.arduino_err;
		((TextView)mv.findViewById(R.id.arduino)).setText(arduino);
		
	}
	static void show_android(ImageManipulationsActivity mv){
		android="アンドロイド！"+"\n"+uart.android+uart.android_err;//+color_ditection.HSV_min[0]
		((TextView)mv.findViewById(R.id.android)).setText(android);
		
	}
    static void set_TextBox(ImageManipulationsActivity mv,int h1,int s1,int v1,int h2,int s2,int v2){
    	Log.e("voice_command","コマンド入力しました");
    	((EditText)mv.findViewById(R.id.H_min)).setText(Integer.toString(h1));
    	((EditText)mv.findViewById(R.id.S_min)).setText(Integer.toString(s1));
    	((EditText)mv.findViewById(R.id.V_min)).setText(Integer.toString(v1));
       	((EditText)mv.findViewById(R.id.H_max)).setText(Integer.toString(h2));
    	((EditText)mv.findViewById(R.id.S_max)).setText(Integer.toString(s2));
    	((EditText)mv.findViewById(R.id.V_max)).setText(Integer.toString(v2));
    }
    
    static void set_ProgressBar(ImageManipulationsActivity mv,int h1,int s1,int v1,int h2,int s2,int v2){
    	Log.e("voice_command","コマンド入力しました");
    	((SeekBar)mv.findViewById(R.id.H_min)).setProgress(h1);
    	((SeekBar)mv.findViewById(R.id.S_min)).setProgress(s1);
    	((SeekBar)mv.findViewById(R.id.V_min)).setProgress(v1);
       	((SeekBar)mv.findViewById(R.id.H_max)).setProgress(h2);
    	((SeekBar)mv.findViewById(R.id.S_max)).setProgress(s2);
    	((SeekBar)mv.findViewById(R.id.V_max)).setProgress(v2);
    }
	private static void orientation(ImageManipulationsActivity mv){//傾き
		CheckBox checkBox = (CheckBox) mv.findViewById(R.id.orientation);
		if(checkBox.isChecked()){
			orientation="傾き"+ "[degree]\n"+
					"X="+	 df1.format(Math.toDegrees(ImageManipulationsActivity.orientationValues[0])+180.0) + 
					"("+  df1.format(machine_ctrl.mDegree)+","+df1.format(machine_ctrl.mAngleVelocity) +")"+"," + 
					"Y="+	 df1.format(Math.toDegrees(ImageManipulationsActivity.orientationValues[1])+180.0) + "," + 
					"Z="+	 df1.format(Math.toDegrees(ImageManipulationsActivity.orientationValues[2])+180.0) +"\n";	
		}
		else orientation="";
	}
	private static void acceleration(ImageManipulationsActivity mv){//加速度
		CheckBox checkBox = (CheckBox) mv.findViewById(R.id.acceleration);
		if(checkBox.isChecked()){
		acceleration="加速度"+"[m/(s*s)]\n"+
					"X="+df1.format(ImageManipulationsActivity.accelerometerValues[0]) + "," + 
					"Y="+df1.format(ImageManipulationsActivity.accelerometerValues[1]) + "," + 
					"Z="+df1.format(ImageManipulationsActivity.accelerometerValues[2]) +"\n";
		}
		else acceleration="";
	}
	private static void magnetic(ImageManipulationsActivity mv){//地磁気
		CheckBox checkBox = (CheckBox) mv.findViewById(R.id.magnetic);
		if(checkBox.isChecked()){
		magnetic="地磁気"+"\n"+
					"X="+df1.format(ImageManipulationsActivity.magneticValues[0]) + "," + 
					"Y="+df1.format(ImageManipulationsActivity.magneticValues[1]) + "," + 
					"Z="+df1.format(ImageManipulationsActivity.magneticValues[2]) +"\n"; 
		}
		else magnetic="";

	}
	private static void color_info(ImageManipulationsActivity mv){//ジャイロ
		CheckBox checkBox = (CheckBox) mv.findViewById(R.id.color_info);
		if(checkBox.isChecked()){
		ditect_info="areaSize="+color_ditection.areaSize+"\n"+
					"X="+(ImageManipulationsActivity.frame_cols/2-color_ditection.areaCenterX)+","+
					"Y="+(ImageManipulationsActivity.frame_rows/2-color_ditection.areaCenterY)+"\n";
		}
		else ditect_info="";
		
	}
	private static void voice(ImageManipulationsActivity mv){
		
		CheckBox checkBox = (CheckBox) mv.findViewById(R.id.voice_recog);
		if(old_start==false&&controller.DualShock3[controller.START]==true)checkBox.setChecked(true);//スタートボタンでも音声認識する
		else if(old_start==true&&controller.DualShock3[controller.START]==false)checkBox.setChecked(false);//スタートボタンを話すとチェックボックスもfalseになる
		
		if(checkBox.isChecked()){
			ImageManipulationsActivity.RecEnable=true;
			voice=mRecognitionListner.voice_command;		
		}
		else{
			ImageManipulationsActivity.RecEnable=false;
			mRecognitionListner.already_strtVR=false;
			voice="";
		}
		
		if(ImageManipulationsActivity.RecEnable==true && mRecognitionListner.already_strtVR==false ){
			mRecognitionListner.startVR();
			mRecognitionListner.already_strtVR=true;
		}
		
		old_start=controller.DualShock3[controller.START];
	}
	private static void seekbar(ImageManipulationsActivity mv){
		seekbar="";
		CheckBox checkBox=(CheckBox)mv.findViewById(R.id.progress_bar);
		if(checkBox.isChecked())
		{
		seekbar+=Integer.toString( ((SeekBar)mv.findViewById(R.id.H_min)).getProgress() )+","
				+Integer.toString(((SeekBar)mv.findViewById(R.id.S_min)).getProgress() )+","
				+Integer.toString(((SeekBar)mv.findViewById(R.id.V_min)).getProgress() )+"\n"
				+Integer.toString(((SeekBar)mv.findViewById(R.id.H_max)).getProgress() )+","
				+Integer.toString(((SeekBar)mv.findViewById(R.id.S_max)).getProgress() )+","
				+Integer.toString(((SeekBar)mv.findViewById(R.id.V_max)).getProgress() )+"\n";
	
		}
	}
	private static void dualshock3(ImageManipulationsActivity mv){//joypad
		String forlog="";
		dualshock3="";
		
		CheckBox checkBox = (CheckBox) mv.findViewById(R.id.dualshock3);
		if(checkBox.isChecked()){
			dualshock3="dualshock3"+"\n"+
				"stick_L="+"("+df1.format(controller.stick_L[0])+","
						  +df1.format(controller.stick_L[1])+","+")"+","+
				"stick_R="+"("+df1.format(controller.stick_R[0])+","
						  +df1.format(controller.stick_R[1])+","+")"+
						  "\n";
	//		if(MainActivity.mIsJoypad)
			{
				 if(controller.DualShock3[controller.Up]){
					 dualshock3+="Up,";
					 forlog+="Up,";
				 }
				 if(controller.DualShock3[controller.Down]){
					 dualshock3+="Down,";
					 forlog+="Down,";
				 }
				 if(controller.DualShock3[controller.Left]){
					 dualshock3+="Left,";
					 forlog+="Left,";
				 }
				 if(controller.DualShock3[controller.Right]){
					 dualshock3+="Right,";
					 forlog+="Right,";
				 }
				 if(controller.DualShock3[controller.Square]){
					 dualshock3+="Square,";
					 forlog+="Square,";
				 }
				 if(controller.DualShock3[controller.Triangle]){
					 dualshock3+="Triangle,";
					 forlog+="Triangle,";
				 }
				 if(controller.DualShock3[controller.Circle]){
					 dualshock3+="Circle,";
					 forlog+="Circle,";
				 }
				 if(controller.DualShock3[controller.Cross]){
					 dualshock3+="Cross,";
					 forlog+="Cross,";
				 }
				 if(controller.DualShock3[controller.L1]){
					 dualshock3+="L1,";
					 forlog+="L1,";
				 }
				 if(controller.DualShock3[controller.R1]){
					 dualshock3+="R1,";
					 forlog+="R1,";
				 }
				 if(controller.DualShock3[controller.L2]){
					 dualshock3+="L2,";
					 forlog+="L2,";
				 }
				 if(controller.DualShock3[controller.R2]){
					 dualshock3+="R2,";
					 forlog+="R2,";
				 }
				 if(controller.DualShock3[controller.L3]){
					 dualshock3+="L3,";
					 forlog+="L3,";
				 }
				 if(controller.DualShock3[controller.R3]){
					 dualshock3+="R3,";
					 forlog+="R3,";
				 }
				 if(controller.DualShock3[controller.SELECT]){
					 dualshock3+="SELECT,";
					 forlog+="SELECT,";
				 }
				 if(controller.DualShock3[controller.START]){
					 dualshock3+="START,";
					 forlog+="START,";
				 }
				 if(controller.DualShock3[controller.PS]){
					 dualshock3+="PS";
					 forlog+="PS";
				 }
				 dualshock3+="\n";
				 Log.d("joystick",forlog);
			}
		}
		else dualshock3="";
		
	}

	
}
