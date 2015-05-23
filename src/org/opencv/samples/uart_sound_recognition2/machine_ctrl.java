package org.opencv.samples.uart_sound_recognition2;

import android.hardware.Camera;



public class machine_ctrl {
	static float duty_L,duty_R;//LRタイヤのduty
	static float choose_Degree=0;//絶対座標表示の正面(degree)
	static float direction_Degree;//目標方向
	static float manual_kaiten;//旋回半径の大きさ（値がデカイほど小回り-100~100）
	static float mDegree;//マシンの絶対角度(degree)
	static float old_mDegree;
	static float mAngleVelocity;//マシンの角速度(degree/sec)
	static float old_rad;
	static int Degree_counter=0;
	static String ctrl_debug="";
	static int 目標方角=0;
	
	static boolean auto_mode=false;//自動走行モード
	static boolean color_chase=true;//色検出モード
	static boolean old_auto_mode=false;
	static boolean one_stick_mode=false;//角度を使った1スティックでの手動走行モード(回転に右スティックを使う)
	static boolean old_select,old_R3;//PSで手動自動、セレクトでスティックのモード切り替え。
	static boolean finish_sound_flag=false;
	static boolean ライト=false;
		

	static void get_absolute_degree(){
		float rad=ImageManipulationsActivity.orientationValues[0];
		if((rad-old_rad) > Math.PI)Degree_counter--;
		else if((rad-old_rad) < -Math.PI)Degree_counter++;
		else Degree_counter+=0;
		mDegree=-(float)(Math.toDegrees(ImageManipulationsActivity.orientationValues[0])+360.0*Degree_counter);
		old_rad=ImageManipulationsActivity.orientationValues[0];
	}
	
	static void get_angle_velocity(){
		mAngleVelocity=(float) ((mDegree-old_mDegree)*100.0);
		old_mDegree=mDegree;
	}
	
	static void wheels_ctrl(){//uartで呼んでいる
		
		//boolean型を反転してトグルにしている
		//if(old_PS==false&&controller.DualShock3[controller.PS]==true)auto_mode=!auto_mode;
		if(old_select==false&&controller.DualShock3[controller.SELECT]==true)auto_mode=!auto_mode;//one_stick_mode=!one_stick_mode;
		
		duty_L=duty_R=0;
		
		if(auto_mode){
			ctrl_debug="auto_mode:n";
			if(color_chase){色追跡();ctrl_debug+="色追跡\n";}
			else {方角へ向く();ctrl_debug+="方角\n";}
			
			//音が重複再生されないため
			if(duty_L==0&&duty_R==0)finish_sound_flag=true;
			else finish_sound_flag=false;
			
			if(!finish_sound_flag)ImageManipulationsActivity.sound_effect.play(ImageManipulationsActivity.chase_finish_id, 1.0F, 1.0F, 0, 0, 1.0F);
		}
		/*
		else if(one_stick_mode){
			one_stick_ctrl();
			ctrl_debug="one_stick_mode\n";
		}
		*/
		else{
			手動操縦();
			ctrl_debug="normal_mode\n";
		}
		
		音声再生();//モードが変わるたびに音を鳴らす
		//ライトのオンオフ();
		

		
		
	}
	private static void 手動操縦(){
		if(Math.abs(controller.lY*100)>15)duty_L=controller.lY*100;
		if(Math.abs(controller.rY*100)>15)duty_R=controller.rY*100;
	}
	
	private static void 色追跡(){
		int speed=100;
		int diff=ImageManipulationsActivity.frame_cols/2-color_ditection.areaCenterX;
		if(diff<-150){//右回転
			//duty_L=-speed/2;
			duty_R=speed/3;
			if(color_ditection.areaSize>60000){
				duty_L=-speed/2;//近づき過ぎたら後退
				duty_R=-speed/4;
			}
			
		}
		else if(diff>150){//左回転
			duty_L=speed/3;
			//duty_R=-speed/2;
			if(color_ditection.areaSize>60000){
				duty_L=-speed/4;
				duty_R=-speed/2;//近づき過ぎたら後退
			}
		}
		else{ 
			duty_L=duty_R= (float)(speed*0.85);//前進;
			if(color_ditection.areaSize>20000)duty_L=duty_R=speed/3;//ある程度近づいたら速度落とす
			if(color_ditection.areaSize>60000)duty_L=duty_R=-speed/3;//近づき過ぎたら後退
			
		}

		if(color_ditection.areaSize<60000&&color_ditection.areaSize>=35000){//以上で止まる＆音声鳴らす
			duty_L=duty_R=0;
		}
		else if(color_ditection.areaSize<500){//検出したサイズが小さかったら見つかるまで反時計回転
			duty_L=-speed/3;
			duty_R=speed/3;
		}
		
		if(duty_R<0){//右タイヤのバックが弱いので。
			duty_R*=1.3;
			if(duty_R<-100)duty_R=-100;
		}

		
		duty_L*=-1;
		duty_R*=-1;
	}
	private static void 方角へ向く(){//差が+だったら時計回り，-だったら反時計回り
		float 今の角度=(float) (Math.toDegrees(ImageManipulationsActivity.orientationValues[0])+180.0);
		float 差=0;//差は-180から180の間になるはず
		if(目標方角==0){
			if(今の角度>180)差=360-今の角度;
			else 差=-今の角度;
		}
		else if(目標方角==90){
			if(今の角度>270)差=360-今の角度+90;
			else 差=90-今の角度;
		}
		else if(目標方角==180){
			差=180-今の角度;
		}
		else if(目標方角==270){
			if(今の角度<90)差=-今の角度-90;
			else 差=270-今の角度;
		}
		
		if(差>30){//時計回り
			duty_L=30;
			duty_R=-30;
		}
		else if(差<-30){//反時計回り
			duty_L=-30;
			duty_R=30;
		}
		else {
			duty_L=0;
			duty_R=0;
		}
		duty_L*=-1;
		duty_R*=-1;
	}
/*	
	private static void one_stick_ctrl(){
		final int max_turn_u=35;//角度フィードバックのMAX値
		final int max_turn_d=20;
		final int ignore_range=15;//パッドのしきい値(傾き15までは無視)
		
		
		float duty_turn = 0;
		float direction_duty=(float) (100.0*(controller.stick_L[0]-ignore_range)/(100-ignore_range));
		
		if(controller.DualShock3[controller.R3])choose_Degree=mDegree;//今向いている方向を格納
		
		direction_Degree=(float) (Math.toDegrees(controller.stick_L[1])-90.0);//スティックの傾いてる方向(degree)
		if(direction_Degree<0)direction_Degree+=360.0;//上に傾けたら0°になるようにした。	
		
		float diff_direction=(float) ((mDegree-choose_Degree)%360-direction_Degree);//向いている方向と目標方向の差
		if(controller.DualShock3[controller.Cross])diff_direction+=180.0;//×押されたら方向と逆を向く
		while(Math.abs(diff_direction)>180.0){
			if(diff_direction<0)diff_direction+=360.0;
			else diff_direction-=360.0;
		}
		
		manual_kaiten=(float) (controller.rX*100.0)/2;//右回りがプラス?
		if(controller.DualShock3[controller.Cross])manual_kaiten*=-1;
		
		if(controller.stick_L[0]>ignore_range ||Math.abs(manual_kaiten)>20){
			//左スティックが倒されたらorR2押されたら
			
			//手動で旋回する時
			if(Math.abs(manual_kaiten)>20)duty_turn=manual_kaiten;
			else if(Math.abs(diff_direction)>30.0){//5°以上ずれていたら
				//回転だけして角度を合わせる
				direction_duty=0;
				duty_turn=(float) (diff_direction*1);//単純なP制御
				if(duty_turn>max_turn_u)duty_turn=max_turn_u;
				if(duty_turn<-max_turn_u)duty_turn=-max_turn_u;
			}
			else{
				//直進しながらも角度のフィードバックをする
				duty_turn=(float) (diff_direction*0.5);//単純なP制御
				if(duty_turn>max_turn_d)duty_turn=max_turn_d;
				if(duty_turn<-max_turn_d)duty_turn=-max_turn_d;
			}
					
		}
		
		//ctrl_debug=""+(int)duty_turn+","+(int)direction_duty+"\n";
		turn_direction_to_duty(duty_turn,direction_duty);
		//ctrl_debug+=""+(int)duty_L+","+(int)duty_R+"\n";
		
	}
	
	private static void turn_direction_to_duty(float turn,float direction_duty){
		
		float L,R;
		L=-(float) (direction_duty+turn);
		R=-(float) (direction_duty-turn);
		if(controller.DualShock3[controller.Cross]){
			L=-(float) (-direction_duty+turn);
			R=-(float) (-direction_duty-turn);
		}
		
		if(L>100.0){R-=L-100;L=100;}
		else if(L<-100){R+=-100-L;L=-100;}
		else if(R>100.0){L-=R-100;R=100;}
		else if(R<-100){L+=-100-R;R=-100;}
		
		if(L>100)L=100;
		if(R>100)R=100;
		if(L<-100)L=-100;
		if(R<-100)R=-100;
		
		duty_L=L;
		duty_R=R;
		
	}
*/	
	private static void 音声再生(){
		if(old_auto_mode==true && auto_mode==false)//モードが変わるたびに音声を鳴らす
			ImageManipulationsActivity.sound_effect.play(ImageManipulationsActivity.a2m_id, 1.0F, 1.0F, 0, 0, 1.0F);
		else if(old_auto_mode==false && auto_mode==true)
			ImageManipulationsActivity.sound_effect.play(ImageManipulationsActivity.m2a_id, 1.0F, 1.0F, 0, 0, 1.0F);
		
		//old_PS=controller.DualShock3[controller.PS];
		old_select=controller.DualShock3[controller.SELECT];
		old_auto_mode=auto_mode;
	}
	private static void ライトのオンオフ(){
		if(old_R3==false && controller.DualShock3[controller.R3]){//R3が押されたら呼び出される
			ライト=!ライト;
			//カメラを取得
			Camera c = Camera.open();
			//カメラのパラメータを取得
			Camera.Parameters cp = c.getParameters();

			if(ライト)cp.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			else cp.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			//パラメータを設定
			c.setParameters(cp);
			//プレビューをしないと光らない
			//c.startPreview();
		}
		old_R3=controller.DualShock3[controller.R3];

	}
}
