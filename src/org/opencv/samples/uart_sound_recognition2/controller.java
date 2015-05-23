package org.opencv.samples.uart_sound_recognition2;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;


public class controller {
	static float lX,lY,rX,rY;//ジョイスティックの値
	static float[] stick_L= new float[2];//極座標(R[0.0~100.0],θ[rad])
	static float[] stick_R= new float[2];
	static boolean[] DualShock3=new boolean[17];
	
	static final int Up=0;
	static final int Down=1;
	static final int Left=2;
	static final int Right=3;
	static final int Circle=4;
	static final int Cross=5;
	static final int Triangle=6;
	static final int Square=7;
	static final int L1=8;
	static final int R1=9;
	static final int L2=10;
	static final int R2=11;
	static final int L3=12;
	static final int R3=13;
	static final int SELECT=14;
	static final int START=15;
	static final int PS=16;
	
	static void joystick_operation(MotionEvent event){
		lX=event.getAxisValue(MotionEvent.AXIS_X);// -1.0 (left) to 1.0 (right)
		lY=event.getAxisValue(MotionEvent.AXIS_Y);//-1.0 (up or far) to 1.0 (down or near)
		rX=event.getAxisValue(MotionEvent.AXIS_Z);
		rY=event.getAxisValue(MotionEvent.AXIS_RZ);
		
		stick_L=XY_to_RTheta(lX,lY);
		stick_R=XY_to_RTheta(rX,rY);
	}
	static void key_operation(KeyEvent ev){// キーコード格納
		
		DualShock3_info(ev.getScanCode());
		if (ev.getAction() == KeyEvent.ACTION_DOWN) {
			// Action Downの時の処理 
		}		
		else if (ev.getAction() == KeyEvent.ACTION_UP) {//離された瞬間
			DualShock3_leave_info(ev.getScanCode());
		}
		
	}
	private static void DualShock3_leave_info(int scancode){
		switch(scancode){
			case 0x124:
				DualShock3[Up]=false;
				break;
			case 0x126:
				DualShock3[Down]=false;
				break;
			case 0x127:
				DualShock3[Left]=false;
				break;
			case 0x125:
				DualShock3[Right]=false;
				break;
			case 0x12d:
				DualShock3[Circle]=false;
				break;
			case 0x12e:
				DualShock3[Cross]=false;
				break;
			case 0x12c:
				DualShock3[Triangle]=false;
				break;
			case 0x12f:
				DualShock3[Square]=false;
				break;
			case 0x12a:
				DualShock3[L1]=false;
				break;
			case 0x12b:
				DualShock3[R1]=false;
				break;
			case 0x0128:
				DualShock3[L2]=false;
				break;
			case 0x0129:
				DualShock3[R2]=false;
				break;
			case 0x0121:
				DualShock3[L3]=false;
				break;
			case 0x0122:
				DualShock3[R3]=false;
				break;
			case 0x0120:
				DualShock3[SELECT]=false;
				break;
			case 0x0123:
				DualShock3[START]=false;
				break;
			case 0x2d0:
				DualShock3[PS]=false;
				break;
		}
		
	}
	private static void DualShock3_info(int scancode){
		switch(scancode){
			case 0x124:
				DualShock3[Up]=true;
				Log.d("DS3_info","Up");
				break;
			case 0x126:
				DualShock3[Down]=true;
				Log.d("DS3_info","Down");
				break;
			case 0x127:
				DualShock3[Left]=true;
				Log.d("DS3_info","Left");
				break;
			case 0x125:
				DualShock3[Right]=true;
				Log.d("DS3_info","Right");
				break;
			case 0x12d:
				DualShock3[Circle]=true;
				Log.d("DS3_info","Circle");
				break;
			case 0x12e:
				DualShock3[Cross]=true;
				Log.d("DS3_info","Cross");
				break;
			case 0x12c:
				DualShock3[Triangle]=true;
				Log.d("DS3_info","Triangle");
				break;
			case 0x12f:
				DualShock3[Square]=true;
				Log.d("DS3_info","Square");
				break;
			case 0x12a:
				DualShock3[L1]=true;
				Log.d("DS3_info","L1");
				break;
			case 0x12b:
				DualShock3[R1]=true;
				Log.d("DS3_info","R1");
				break;
			case 0x0128:
				DualShock3[L2]=true;
				Log.d("DS3_info","L2");
				break;
			case 0x0129:
				DualShock3[R2]=true;
				Log.d("DS3_info","R2");
				break;
			case 0x0121:
				DualShock3[L3]=true;
				Log.d("DS3_info","L3");
				break;
			case 0x0122:
				DualShock3[R3]=true;
				Log.d("DS3_info","R3");
				break;
			case 0x0120:
				DualShock3[SELECT]=true;
				Log.d("DS3_info","SELECT");
				break;
			case 0x0123:
				DualShock3[START]=true;
				Log.d("DS3_info","START");
				break;
			case 0x2d0:
				DualShock3[PS]=true;
				Log.d("DS3_info","PS");
				break;
		}
		
	}
	private static float[] XY_to_RTheta(float x,float y){
		//x:-1.0 (left) to 1.0 (right)
		//y:-1.0 (up or far) to 1.0 (down or near)
		  float data[] = new float[2];
		  
		  data[0] = (float) (Math.sqrt(x*x+y*y)*100.0);
		  if(data[0]>100)data[0]=(float) 100.0;
		  
		  data[1] = (float) Math.atan2(-y, -x);
		  data[1]+=Math.PI;//0~2πにする
		  data[1]=(float) (2.0*Math.PI-data[1]);
		  
		  return data;
	}
	
}
