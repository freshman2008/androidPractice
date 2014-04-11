package com.edward.mymusicplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button play;
	private Button stop;
	private Button prev;
	private Button next;
	private Button exit;
	private Button mode;
	private TextView name;
	private TextView time1;
	private TextView time2;
	private LinearLayout layout;
	private MediaPlayer mediaPlayer;
	private ListView list;
	private SeekBar progressbar;
    private ArrayList<HashMap<String, Object>> listItem;
    private ArrayList<String> listStringItem;
    private int curIndex=0;
    private int playMode=0;//0:single 1:cycle 2:random
	 /*时间格式转换*/
    public String toTime(int time) {
 
        time /= 1000;
        int minute = time / 60;
        //int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }
    
    
	/**
	 * 
	 * 功能 通过album_id查找 album_art 如果找不到返回null
	 * 
	 * @param album_id
	 * @return album_art
	 */
	private String getAlbumArt(int album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = this.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
				projection, null, null, null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;
		return album_art;
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
    
    
    
    
    public void playMusic(int index) {
       	Uri myUri = Uri.parse(listItem.get(index).get("Path").toString());// initialize Uri here
    	if(mediaPlayer != null) {
    		mediaPlayer.stop();
    		mediaPlayer.release();
    		mediaPlayer = null;
    	}
    	Bitmap bm = null;
    	BitmapDrawable bmpDraw;
    	String albumArt = getAlbumArt(Integer.decode(listItem.get(index).get("Album").toString()));  
    	if(albumArt != null) {
    		bm = BitmapFactory.decodeFile(albumArt);
        	bmpDraw = new BitmapDrawable(bm);
        	layout.setBackgroundDrawable(bmpDraw.getCurrent());
    	} else {
    		layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_launcher));
    	}
    	
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
    	name.setText(listItem.get(index).get("Name").toString());
    	progressbar.setMax(mediaPlayer.getDuration());
    	time1.setText(toTime(mediaPlayer.getDuration()));
    	time2.setText(toTime(mediaPlayer.getDuration()));
    	mediaPlayer.start();
    	play.setText(R.string.Pause);
    	handler.post(updateThread);
    	// 设置播放完毕监听
    	mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
    	    @Override
    	    public void onCompletion(MediaPlayer mp) {
    	    	switch(playMode) {
            	case 0:
            		playMusic(curIndex);
            		break;
            	case 1:
            		playNext();
            		break;
            	case 2:
            	default:
	            	{
	            		curIndex=(int)(Math.random() * 6);
	            		playMusic(curIndex);
	            	}
            		break;
            	}
    	    }
    	});
    }
    
    public void playNext() {
    	int index = list.getChildCount();
    	if(curIndex == index) {
    		curIndex = 0;
    	} else {
    		++curIndex;
    	}
    	playMusic(curIndex);
    }
    
    public void playPrev() {
    	int index = list.getChildCount();
    	if(curIndex == 0) {
    		curIndex = index;
    	} else {
    		--curIndex;
    	}
    	playMusic(curIndex);
    }
    
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		layout=(LinearLayout) findViewById(R.id.main_layout);
		//layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_launcher));
		
		list = (ListView)findViewById(R.id.ListView01);
		progressbar = (SeekBar)findViewById(R.id.progress);
        listItem = new ArrayList<HashMap<String, Object>>();
        listStringItem = new ArrayList<String>();
        
		
		//生成动态数组，加入数据  
		Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,	null, null,	null, 
									   MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor.moveToFirst()) {
			while(cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)); 
				//String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				HashMap<String, Object> map = new HashMap<String, Object>();  
	            map.put("Name", name);  
	            map.put("Path", path); 
	            map.put("Album", album_id);
	            listItem.add(map);	
	            listStringItem.add(name);
			}	
		}
		//list.setAdapter(new ArrayAdapter<HashMap<String, Object>>(this, android.R.layout.simple_expandable_list_item_1,listItem));
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listStringItem));
		list.setOnItemClickListener(new OnItemClickListener() {  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //Toast.makeText(getApplicationContext(), ""+url.get(arg2), Toast.LENGTH_LONG).show();
            	playMusic(arg2);
            	curIndex = arg2;
            }  
        }); 
		
		play = (Button)findViewById(R.id.play);
		prev = (Button)findViewById(R.id.Prev);
		next = (Button)findViewById(R.id.Next);
		stop = (Button)findViewById(R.id.stop);
		exit = (Button)findViewById(R.id.Exit);
		mode = (Button)findViewById(R.id.Mode);
		name = (TextView)findViewById(R.id.music_name);
		time1 = (TextView)findViewById(R.id.time_1);
		time2 = (TextView)findViewById(R.id.time_2);
		
		mode.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	switch(playMode) {
            	case 0:
            		mode.setText(R.string.Mode2);
            		playMode = 1;
            		break;
            	case 1:
            		mode.setText(R.string.Mode3);
            		playMode = 2;
            		break;
            	case 2:
            	default:
            		mode.setText(R.string.Mode1);
            		playMode = 0;
            		break;
            	}
            }
        });
		
		play.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	if(mediaPlayer.isPlaying()) {
            		mediaPlayer.pause();
            		play.setText(R.string.Play);
            		
            	} else {
            		mediaPlayer.start();
            		play.setText(R.string.Pause);
            	}
            }
        });
		
		prev.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	playPrev();
            }
        });
		
		next.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	playNext();
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
		progressbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private int value = 0;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mediaPlayer.seekTo(value);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser) {
					value = progress;
					//mediaPlayer.seekTo(progress);				
				}
			}
		});	
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
