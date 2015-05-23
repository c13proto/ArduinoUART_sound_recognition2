package org.opencv.samples.uart_sound_recognition2;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import android.widget.SeekBar;



public class color_ditection{
	//色検出のための型
	static Mat resized=new Mat();//before_changedのリサイズ版
	static Mat mask=new Mat();//マスク
    static Mat mRgba= new Mat();//マスク処理後の画像
    static Mat mHSV2= new Mat();//グレースケール
    static Mat mHSV= new Mat();
    
    //領域分割（ラベリング）のための型

    static int areaSize=0;//検出するエリアの最大のものの面積
    static int areaIndex=-1;
    static int areaCenterX=0,areaCenterY=0;//最大エリアの座標
    
    
    static double center_HSV[]=new double[3];//画面中央のrgba値取得
	static int HSV_min[]=new int[3];
	static int HSV_max[]=new int[3];
    
    static Mat ditect_colors(ImageManipulationsActivity mv,Mat before_changed,int ditect_color){
    	
    	int data_check=0;//HSVに正しい値が入っているかチェック 
    	
    	
    	HSV_min[0]=( (SeekBar)mv.findViewById(R.id.H_min)).getProgress();
    	HSV_min[1]=( (SeekBar)mv.findViewById(R.id.S_min)).getProgress();
    	HSV_min[2]=( (SeekBar)mv.findViewById(R.id.V_min)).getProgress();
    	
    	HSV_max[0]=( (SeekBar)mv.findViewById(R.id.H_max)).getProgress();
    	HSV_max[1]=( (SeekBar)mv.findViewById(R.id.S_max)).getProgress();
    	HSV_max[2]=( (SeekBar)mv.findViewById(R.id.V_max)).getProgress();
    	
//    	for(int i=0;i<3;i++){//数値でなければ外す
//    		if(!check(HSV_min[i]))data_check++;
//    		if(!check(HSV_max[i]))data_check++;
//    	}
    	if(data_check==0){
    		//Log.d("ストリングチェック通過したよ","HSV_min="+HSV_min[0]);
        	for(int i=0;i<3;i++){//0~255に入っていなければ外す
        		if((HSV_min[i])>255 || (HSV_min[i])<0)data_check++;
        		if((HSV_max[i])>255 || (HSV_max[i])<0)data_check++;
        		if(i==0&&(HSV_min[i])>180)data_check++;
        		if(i==0&&(HSV_max[i])>180)data_check++;
        	}
       	}
    	
    	
    	
    	//before_changed.copyTo(mRgba);//mRgba=before_changed;だとbefore_changedもmRgbaと共に変化したｗ
        Imgproc.cvtColor(before_changed, mHSV, Imgproc.COLOR_RGB2HSV,3);//HSVへの変換。BGRだとHの指定の時にズレる
        
        
        if(data_check==0){
        	Core.inRange(mHSV, new Scalar((HSV_min[0]),(HSV_min[1]),(HSV_min[2])),
        					   new Scalar((HSV_max[0]),(HSV_max[1]),(HSV_max[2])), mHSV2);
        }
        else
        {
	    	switch (ditect_color) {
	        case 1://赤
	        	Core.inRange(mHSV, new Scalar(0, 100, 30), new Scalar(5, 255, 255), mHSV2);
	        	//set_TextBox(mv,0,100,300,5,255,255);
	        	mRecognitionListner.voice_command="赤";
	            break;
	        case 2://青
	        	Core.inRange(mHSV, new Scalar(90, 50, 50), new Scalar(125, 255, 255), mHSV2);
	        	//set_TextBox(mv,90,50,50,125,255,255);
	        	mRecognitionListner.voice_command="青";
	            break;
	        case 3://緑
	        	Core.inRange(mHSV, new Scalar(50, 50, 50), new Scalar(90, 255, 255), mHSV2);
	        	//set_TextBox(mv,50,50,50,90,255,255);
	        	mRecognitionListner.voice_command="緑";
	            break;
	        case 4://黄色
	        	Core.inRange(mHSV, new Scalar(20, 50, 50), new Scalar(40, 255, 255), mHSV2);
	        	//set_TextBox(mv,20,50,50,40,255,255);
	        	mRecognitionListner.voice_command="黄色";
	            break;
	        case 5://肌色
	        	Core.inRange(mHSV, new Scalar(0, 75, 89), new Scalar(20, 192, 243), mHSV2);
	        	//set_TextBox(mv,0,38,89,20,192,243);
	        	mRecognitionListner.voice_command="肌色";
	            break;
	    	}
        }
        
        //グレースケールのHSVから表示できる形式に変換
    	//Imgproc.cvtColor(mHSV2, mRgba, Imgproc.COLOR_GRAY2BGR, 0);
        //Imgproc.cvtColor(mRgba, mRgba2, Imgproc.COLOR_BGR2BGRA, 4);
        
        Imgproc.cvtColor(mHSV2, mask, Imgproc.COLOR_GRAY2RGBA, 4);//これ1行でよくね？
        
        //ノイズ除去
        Imgproc.erode(mask,mask,Mat.ones(5,5,CvType.CV_8UC1));//maskはRGBAのマスク画像
        //Imgproc.dilate(mask,mask,Mat.ones(5,5,CvType.CV_8UC1));
        
        //デカイ領域探し
        rabeling(mHSV2);//グレースケールのものを渡さなければいけないっぽい
        
        mRgba.release();//これをしないと画像が重なる。ここでnewすると恐らくメモリリークで10秒位で落ちる
        //Core.bitwise_and(before_changed,mask,mRgba);マスクかけるならどっちでもOKっぽい
        before_changed.copyTo(mRgba, mask);//第2引数にマスクのmatを入れることができる。
        //resized.copyTo(mRgba, mask);
        
        return mRgba;//元画像にマスクされた画像を返す
    } 
    
    static boolean check(String str) {//数値化どうかチェック
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
    
    static void get_center_hsv(Mat rgba){
    	Mat hsv = new Mat();
        Imgproc.cvtColor(rgba, hsv, Imgproc.COLOR_RGB2HSV,3);//HSV変換したものをhsvへ保存
        center_HSV=hsv.get(hsv.height()/2,hsv.width()/2);//真ん中のHSV値取得
        hsv.release();
    }
    
    private static void rabeling(Mat BinMask){
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
    	Imgproc.findContours(BinMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
    	
    	int ditect_size=0;
    	int index=-1;//エリアID
    	int X=0;
    	int Y=0;
    	
    	for(int i=0;i<contours.size();i++){
    		Mat contour=contours.get(i);//i番目の輪郭の情報を格納
    		
    		int[] point={0,0};
    		int lx,rx,ty,by;
    		
    		contour.get(0,0,point);
    		lx=rx=point[0];
    		ty=by=point[1];
    		
    		for(int r=0;r<contour.rows();r++){
    			contour.get(r,0,point);
    			if(point[0]<lx)lx=point[0];
    			else if(point[0]>rx)rx=point[0];
    			if(point[1]<ty)ty=point[1];
    			else if(point[1]>by)by=point[1];
    		}
    		
    		int size=(rx-lx)*(by-ty);//byのが座標が高い。数え方が↓→なので。
    		if(size>ditect_size){
    			ditect_size=size;
    			index=i;
    			X=(lx+rx)/2;
    			Y=(ty+by)/2;
    		}
    		
    	}
    	
    	areaIndex=index;
    	areaCenterX=X*ImageManipulationsActivity.split_pixel;
    	areaCenterY=Y*ImageManipulationsActivity.split_pixel;
    	areaSize=ditect_size*ImageManipulationsActivity.split_pixel*ImageManipulationsActivity.split_pixel;
    	
    	contours.clear();
    	hierarchy.release();
    }
    

}
