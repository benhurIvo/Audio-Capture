package com.ivo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
 
 
public class MainActivity extends Activity {
    EditText niknem;
	AlertDialog.Builder myDialog;
	FileOutputStream os = null;
   // private static final int RECORDER_SAMPLERATE = 8000;
    //private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    //private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private PendingIntent pendingIntent;
	
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    final Context context = this;
	String data2send="";
	private int bufferSize = 0;
	List<String> mydata = null;
	private static final String AUDIO_RECORDER_FOLDER = "benhurRecorder";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        
        setButtonHandlers();
        enableButtons(false);

        bufferSize = AudioRecord.getMinBufferSize(44100,
        		AudioFormat.CHANNEL_IN_MONO,
        		AudioFormat.ENCODING_PCM_16BIT);
    }

    //Button handlers
    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    //Get the file name
    private String getFilename(String nem){
    	String filepath = Environment.getExternalStorageDirectory().getPath();
    	File file = new File(filepath,AUDIO_RECORDER_FOLDER);

    	if(!file.exists()){
    	file.mkdirs();
    	}

    	return (file.getAbsolutePath() + File.separator + nem+"~"+System.currentTimeMillis()+".txt");
    	}

    	
    
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 128; // 2 bytes in 16bit format

    private void startRecording(final String fnem) {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile(fnem);
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

        private void writeAudioDataToFile(String sss){
    	byte data[] = new byte[bufferSize];
    	String filename = getFilename(sss);

    	try {
    	os = new FileOutputStream(filename);
    	} catch (FileNotFoundException e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    	}

    	int read = 0;

    	if(null != os){
    	while(isRecording){
    	read = recorder.read(data, 0, bufferSize);

    	if(AudioRecord.ERROR_INVALID_OPERATION != read){
    	try {
    	os.write(data);
    	} catch (IOException e) {
    	e.printStackTrace();
    	}
    	}
    	
    	}
    	}
    	}

    private void stopRecording() {
    	if(os!=null)
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
		        
    } 
    
  
    //Dialog to enter name of audio file
    	private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btnStart: {
            	
            	AlertDialog.Builder sayWindows = new AlertDialog.Builder(
    		            MainActivity.this);
    		    sayWindows.setTitle("Audio Name");
    		    sayWindows.setPositiveButton("ok", null);
    		    sayWindows.setNegativeButton("cancel", null);
    	    	TextView viw = new TextView(context);
    		    viw.setText("Name");
    		    niknem = new EditText(context);
    	    	LinearLayout layout = new LinearLayout(context);
    			layout.setOrientation(LinearLayout.VERTICAL);
    		    
    			layout.addView(viw);
    			layout.addView(niknem);
    		    
    			sayWindows.setView(layout);

    		    final AlertDialog mAlertDialog = sayWindows.create();
    		    mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

    		        @Override
    		        public void onShow(DialogInterface dialog) {

    		            Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    		            b.setOnClickListener(new View.OnClickListener() {

    		                @Override
    		                public void onClick(View view) {
    		                  
    		            String nn = niknem.getText().toString().trim();
    		            if(nn.length()>2){
    		            //Toast.makeText(MainActivity.this, nn, Toast.LENGTH_SHORT).show();
    		            enableButtons(true);
    	                start();
    	                startRecording(nn);
    		            mAlertDialog.dismiss();
    		            }
    		            
    		                	   
    		                   
    		                }
    		            });
    		        }
    		    });
    		    mAlertDialog.show();
                break;
            }
            case R.id.btnStop: {
                enableButtons(false);
                cancel();
                stopRecording();
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                break;
            }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    
     
//Start recording, and also send a notification    
    public void start() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 8000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        //Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        //Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }
 
}
