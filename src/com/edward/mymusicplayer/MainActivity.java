package com.edward.mymusicplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button play;
	private Button stop;
	private Button exit;
	private TextView name;
	private TextView time1;
	private TextView time2;
	private MediaPlayer mediaPlayer;
	private ListView list;
	private ProgressBar progressbar;
    private ArrayList<HashMap<String, Object>> listItem;
    private ArrayList<String> listStringItem;
	 /*时间格式转换*/
    public String toTime(int time) {
 
        time /= 1000;
        int minute = time / 60;
        //int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }
    
	Handler handler = new Handler();  
    Runnable updateThread = new Runnable() {  
        public void run() {  
            //获得歌曲现在播放位置并设置成播放进度条的值  
        	progressbar.setProgress(mediaPlayer.getCurrentPosition());
        	time1.setText(toTime(mediaPlayer.getCurrentPosition()));
            //每次延迟100毫秒再启动线程 
            handler.postDelayed(updateThread, 100);  
        }  
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		list = (ListView)findViewById(R.id.ListView01);
		progressbar = (ProgressBar)findViewById(R.id.progress);
        listItem = new ArrayList<HashMap<String, Object>>();
        listStringItem = new ArrayList<String>();
		
		//生成动态数组，加入数据  
		Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,	null, null,	null, 
									   MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor.moveToFirst()) {
			while(cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)); 
				HashMap<String, Object> map = new HashMap<String, Object>();  
	            map.put("Name", name);  
	            map.put("Path", path);  
	            listItem.add(map);
	            listStringItem.add(name);
			}	
		}
		//list.setAdapter(new ArrayAdapter<HashMap<String, Object>>(this, android.R.layout.simple_expandable_list_item_1,listItem));
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listStringItem));
		
		list.setOnItemClickListener(new OnItemClickListener() {  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //Toast.makeText(getApplicationContext(), ""+url.get(arg2), Toast.LENGTH_LONG).show();
               	Uri myUri = Uri.parse(listItem.get(arg2).get("Path").toString());// initialize Uri here
            	mediaPlayer = new MediaPlayer();
            	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            	try {
					mediaPlayer.setDataSource(getApplicationContext(), myUri);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	try {
					mediaPlayer.prepare();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	name.setText(listItem.get(arg2).get("Name").toString());
            	progressbar.setMax(mediaPlayer.getDuration());
            	time1.setText(toTime(mediaPlayer.getDuration()));
            	time2.setText(toTime(mediaPlayer.getDuration()));
            	mediaPlayer.start();
            	handler.post(updateThread); 
            }  
        }); 
		
		play = (Button)findViewById(R.id.play);
		stop = (Button)findViewById(R.id.stop);
		exit = (Button)findViewById(R.id.Exit);
		name = (TextView)findViewById(R.id.music_name);
		time1 = (TextView)findViewById(R.id.time_1);
		time2 = (TextView)findViewById(R.id.time_2);
		
		play.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
        		mediaPlayer.start();
            }
        });
		
		stop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	mediaPlayer.pause();
            }
        });
		exit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	mediaPlayer.release();
            	mediaPlayer=null;
            }
        });
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
