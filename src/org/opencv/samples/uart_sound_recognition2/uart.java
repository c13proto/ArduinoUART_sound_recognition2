package org.opencv.samples.uart_sound_recognition2;

import java.io.IOException;
import java.text.DecimalFormat;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

public class uart {
	static String arduino="";
	static String arduino_err="";
	static String android="";
	static String android_err="";
	static String android_recieve_average="";
	static String arduino_recieve_average="";
	private final static int send_data_number=10;
	private final static int arduino_data_number=10;
	private final static int recieve_data_number=256;
	private static byte[] send_data = new byte[send_data_number];
	private static byte[] recieve_data = new byte[recieve_data_number];
	private static int recieve_counter=0;
	private static int correct_counter=0;
	static DecimalFormat df1=new DecimalFormat("##0.00");
	
    static void start_read_and_write_thread(final UsbSerialDriver usb){
 

    	new Thread(new Runnable(){
			public void run(){
    	    	
    	    	while(ImageManipulationsActivity.mIsActivity){
    	    		machine_ctrl.wheels_ctrl();
    	    		create_send_data();
	    	    	write_arduino(usb);
	    	    	read_arduino(usb);
	    	    	
    	    	}
    	        try {Thread.sleep(1000);}
    	        catch (InterruptedException e) {
    	              // good practice
    	              Thread.currentThread().interrupt();
    	              return;
    	        }
    	    }
    	  }).start();
    	  
    }
    
    private static void read_arduino(final UsbSerialDriver usb){
	      try{
	    	  recieve_counter++;
  	          int num=usb.read(recieve_data, recieve_data_number);
	    	  if(num==arduino_data_number){ // Arduinoから受信した値を格納

  	        	  String recieved="";
	    		  correct_counter++;//正しく受信できた数を数える
  	        	  after_recieve_operation();
  	        	  for(int i=0;i<arduino_data_number;i++){//アルディーノ側で見えている値にする
  	        		  short unsigned=recieve_data[i];
  	        		  if(unsigned<0)unsigned+=256;
  	        		recieved+=""+unsigned+"\n";
  	        		
  	        		//if(i==9)arduino_recieve_average="Arduino受信成功率:"+df1.format(unsigned)+"%\n";
  	        		
  	        	  }
  	        	  arduino=recieved;
  	        	  arduino_err="";
  	          }
	    	  
	    	  if(recieve_counter>500){
	    		  android_recieve_average="Android受信成功率:"+df1.format(correct_counter*100.0/recieve_counter)+"%\n";
	    		  recieve_counter=0;
	    		  correct_counter=0;
	    	  }
	    	  
  	      }
  	      catch(IOException e){
  	        e.printStackTrace();
  	        arduino_err="recieve_err\n";
  	      }
    }
    
    private static void write_arduino(final UsbSerialDriver usb){
	      try{
	    	  	  String sended="";
	        	  usb.write(send_data,1);
	        	  for(int i=0;i<10;i++){//アルディーノで見えている値にする
	        		  short unsigned=send_data[i];
	        		  if(unsigned<0)unsigned+=256;
	        		  sended+=""+unsigned+"\n";
	        	  }
	        	  android=sended;
	        	  android_err="";
	      }
	      catch(IOException e){
	        e.printStackTrace();
	        android_err="write_err\n";
	      }

    }
    private static void create_send_data(){
    	byte controller1=(byte) 0;
    	byte controller2=(byte) 0;
    	for(int i=0;i<send_data_number;i++)send_data[i]=0;//送るデータの初期化
    	
    	for(int i=0;i<8;i++){//コントローラの値
    		if(controller.DualShock3[i])controller1+=Math.pow(2,i);
    		if(controller.DualShock3[i+8])controller2+=Math.pow(2,i);
    	}
    	send_data[0]=controller1;
    	send_data[1]=controller2;
    	if(controller.DualShock3[controller.PS])send_data[2]=1;
    	send_data[3]=(byte) (controller.lX*100+100);
    	send_data[4]=(byte) (controller.lY*100+100);
    	send_data[5]=(byte) (controller.rX*100+100);
    	send_data[6]=(byte) (controller.rY*100+100);
    	send_data[7]=(byte)(machine_ctrl.duty_L+100);
    	send_data[8]=(byte)(machine_ctrl.duty_R+100);
    	send_data[9]=(byte)255;
    	
    	
    }
    
    private static void after_recieve_operation(){
    	int count=0;
    	byte[] copy=new byte[arduino_data_number];
    	for(int i=0;i<10;i++)if(recieve_data[i]+256==255)count=i;//recieve_data[count]に255が来ている
    	for(int i=0;i<arduino_data_number;i++){
    		copy[(i+arduino_data_number-1)%arduino_data_number]=recieve_data[(count+i)%arduino_data_number];
    	}
    	recieve_data=copy.clone();//順番を整理したものを受信データとして戻す     
    }

}
