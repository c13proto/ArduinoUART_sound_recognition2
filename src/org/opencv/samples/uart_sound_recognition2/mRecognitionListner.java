package org.opencv.samples.uart_sound_recognition2;

import java.util.ArrayList;
import java.util.regex.Pattern;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;

public class mRecognitionListner implements RecognitionListener {

    static String voice_command="";
    static boolean already_strtVR=false;
    static int[] voice_HSV_command=new int[3];
    private static String old_voice_command="";
	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		switch(error){
			case SpeechRecognizer.ERROR_NETWORK:
	    		startVR();
	    		Log.e("mRecognitionListner","ネットワークエラー");
	    		break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
	    		startVR();
	    		Log.e("mRecognitionListner","ネットワークTimeoutエラー");
	    		break;	
			case SpeechRecognizer.ERROR_NO_MATCH:
	    		startVR();
	    		Log.e("mRecognitionListner","no match");
	    		break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
	    		startVR();
	    		Log.e("mRecognitionListner","スピーチタイムアウト");
	    		break;
	    	default:
		
		}
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResults(Bundle results) {
		// TODO Auto-generated method stub
        ArrayList<String> recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String str="";
        String command="nothing";
        boolean hsv_command=false;
        for(String s:recData){
        	str+=s+"";
        	s=s.trim();
        	s=s.replaceAll("[ 　]","");
        	if(s.equals("赤"))command="赤";
        	else if(s.equals("青"))command="青";
        	else if(s.equals("水色"))command="水色";
        	else if(s.equals("黄色"))command="黄色";
        	else if(s.equals("緑"))command="緑";
        	else if(s.equals("白"))command="白";
        	else if(s.equals("肌色"))command="肌色";
        	else if(s.equals("ピンク"))command="ピンク";
        	else if(s.equals("紫"))command="紫";
        	else if(s.equals("東"))command="東";
        	else if(s.equals("西"))command="西";
        	else if(s.equals("南"))command="南";
        	else if(s.equals("北"))command="北";
        	
        	s=s.replaceAll("[A-Za-z]","");
        	if(ishsv_voice_command(s)&&hsv_command==false){//音声でHSVの値を指定．3かける50->Vを50に
        		String place=s.substring(0,1);//最初の文字を取り出す
	        	String number=s.substring(s.lastIndexOf("*")+1);//文字列の*以降から最後までを取り出す
	        			hsv_command=true;
	        			voice_HSV_command[0]=1;
	        			voice_HSV_command[1]=Integer.parseInt(place);
	        			voice_HSV_command[2]=Integer.parseInt(number);
        	}
        }
        
        if(command.equals("nothing"))voice_command=str;
        else voice_command=command;
        if(hsv_command==false) voice_HSV_command[0]=0;
        
        startVR();//音声認識再開
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub

	}
    static void startVR(){//音声認識開始
    	if(ImageManipulationsActivity.RecEnable){
	    	Log.e("startVR","startVR開始!");
	    	try {
	            // インテント作成
	            // 入力した音声を解析する。
	            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
	            // free-form speech recognition.
	            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	            // 表示させる文字列
	            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "calling_package");
	            intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US");
	            // インテント開始
	            ImageManipulationsActivity.mSpeechRecognizer.startListening(intent);
	        }
	    	catch (ActivityNotFoundException e) {
	            // アクティビティが見つからなかった
	    		Log.e("startVR",e.getMessage());
	    	} 
    	}
    }

	static void voice_command_operation(ImageManipulationsActivity mv) {
		// TODO Auto-generated method stub
		boolean command=true;
		machine_ctrl.color_chase=true;
//    	if(voice_command.equals("赤"))show.set_TextBox(mv,160,100,30,180,255,255);
//    	else if(voice_command.equals("青"))show.set_TextBox(mv,100,100,100,140,255,255);
//    	else if(voice_command.equals("水色"))show.set_TextBox(mv,80,50,50,110,255,255);
//    	else if(voice_command.equals("緑"))show.set_TextBox(mv,50,50,50,90,255,255);
//    	else if(voice_command.equals("紫"))show.set_TextBox(mv,120,50,50,150,255,255);
//    	else if(voice_command.equals("ピンク"))show.set_TextBox(mv,140,50,50,170,255,255);
//    	else if(voice_command.equals("黄色"))show.set_TextBox(mv,20,50,50,45,255,255);
//    	else if(voice_command.equals("肌色"))show.set_TextBox(mv,0,38,89,20,192,243);
//    	else if(voice_command.equals("白"))show.set_TextBox(mv,0,0,170,180,55,255);
    	
    	if(voice_command.equals("赤"))show.set_ProgressBar(mv,160,100,30,180,255,255);
    	else if(voice_command.equals("青"))show.set_ProgressBar(mv,100,100,100,140,255,255);
    	else if(voice_command.equals("水色"))show.set_ProgressBar(mv,80,50,50,110,255,255);
    	else if(voice_command.equals("緑"))show.set_ProgressBar(mv,50,50,50,90,255,255);
    	else if(voice_command.equals("紫"))show.set_ProgressBar(mv,120,50,50,150,255,255);
    	else if(voice_command.equals("ピンク"))show.set_ProgressBar(mv,140,50,50,170,255,255);
    	else if(voice_command.equals("黄色"))show.set_ProgressBar(mv,20,50,50,45,255,255);
    	else if(voice_command.equals("肌色"))show.set_ProgressBar(mv,0,75,89,20,192,243);
    	else if(voice_command.equals("白"))show.set_ProgressBar(mv,0,0,170,180,55,255);
    	else {
    		machine_ctrl.color_chase=false;
    		if(voice_command.equals("東"))machine_ctrl.目標方角=270;
	    	else if(voice_command.equals("西"))machine_ctrl.目標方角=90;
	    	else if(voice_command.equals("南"))machine_ctrl.目標方角=0;
	    	else if(voice_command.equals("北"))machine_ctrl.目標方角=180;
	    	else command=false;
    	}
    	
    	if(command&&(old_voice_command!=voice_command))//音声コマンドが変わったら音を鳴らす
    		ImageManipulationsActivity.sound_effect.play(ImageManipulationsActivity.command_changed_id, 1.0F, 1.0F, 0, 0, 1.0F);
    	
    	//voice_HSV_command(mv);
    	voice_HSV_command_forProgressBar(mv);
    	
    	old_voice_command=voice_command;
	}

	static void voice_HSV_command(ImageManipulationsActivity mv){
		
		if(voice_HSV_command[0]==1){
			ImageManipulationsActivity.sound_effect.play(ImageManipulationsActivity.command_changed_id, 1.0F, 1.0F, 0, 0, 1.0F);
			
	    	if(voice_HSV_command[1]==1)((EditText)mv.findViewById(R.id.H_min)).setText(Integer.toString(voice_HSV_command[2]));
	    	else if(voice_HSV_command[1]==2)((EditText)mv.findViewById(R.id.S_min)).setText(Integer.toString(voice_HSV_command[2]));
	    	else if(voice_HSV_command[1]==3)((EditText)mv.findViewById(R.id.V_min)).setText(Integer.toString(voice_HSV_command[2]));
	    	else if(voice_HSV_command[1]==4)((EditText)mv.findViewById(R.id.H_max)).setText(Integer.toString(voice_HSV_command[2]));
	    	else if(voice_HSV_command[1]==5)((EditText)mv.findViewById(R.id.S_max)).setText(Integer.toString(voice_HSV_command[2]));
	    	else if(voice_HSV_command[1]==6)((EditText)mv.findViewById(R.id.V_max)).setText(Integer.toString(voice_HSV_command[2]));
		
	    	voice_HSV_command[0]=0;
		}
			
	}
	static void voice_HSV_command_forProgressBar(ImageManipulationsActivity mv){
		
		if(voice_HSV_command[0]==1){
			ImageManipulationsActivity.sound_effect.play(ImageManipulationsActivity.command_changed_id, 1.0F, 1.0F, 0, 0, 1.0F);
			
	    	if(voice_HSV_command[1]==1)((SeekBar)mv.findViewById(R.id.H_min)).setProgress(voice_HSV_command[2]);
	    	else if(voice_HSV_command[1]==2)((SeekBar)mv.findViewById(R.id.S_min)).setProgress(voice_HSV_command[2]);
	    	else if(voice_HSV_command[1]==3)((SeekBar)mv.findViewById(R.id.V_min)).setProgress(voice_HSV_command[2]);
	    	else if(voice_HSV_command[1]==4)((SeekBar)mv.findViewById(R.id.H_max)).setProgress(voice_HSV_command[2]);
	    	else if(voice_HSV_command[1]==5)((SeekBar)mv.findViewById(R.id.S_max)).setProgress(voice_HSV_command[2]);
	    	else if(voice_HSV_command[1]==6)((SeekBar)mv.findViewById(R.id.V_max)).setProgress(voice_HSV_command[2]);
		
	    	voice_HSV_command[0]=0;
		}
			
	}


    /**
     * 指定した文字列が半角文字のみか判断する
     *
     * @param source 対象文字列
     * @return trueなら半角文字のみ 空の場合は常にtrueとなる
     */
    public boolean isHankakuOnly(String source) {
        if (source == null || source.equals("")) {
            return true;
        }
        String regText = "[ -~｡-ﾟ]+";
        Pattern pattern = Pattern.compile(regText);
        return pattern.matcher(source).matches();
    }
    
    
    private boolean ishsv_voice_command(String s){
    	boolean check = false;
    	if(s.length()<=2)return false;//文字が短すぎるときはfalse
    	else if(isHankakuOnly(s)){
    		if(color_ditection.check(s.substring(0,1))){
    			int place=Integer.parseInt(s.substring(0,1));//最初の文字のチェック
    			if(place>0 && place<7){
    				String s_number=s.substring(s.lastIndexOf("*")+1);//文字列の*以降から最後までを取り出す
    				if(color_ditection.check(s_number)){
    					int i_number=Integer.parseInt(s_number);
    					if(place==1||place==4){
    						if(i_number>=0||i_number<=180)check=true;
    					}
    					else if(i_number>=0&&i_number<=255)check=true;
    				}
    			}
    		}
    	}

    	return check;
    }

}
