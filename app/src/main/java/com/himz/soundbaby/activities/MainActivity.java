package com.himz.soundbaby.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.himz.soundbaby.R;
import com.himz.soundbaby.adapters.SongAdapter;
import com.himz.soundbaby.adapters.SongInfo;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ArrayList<SongInfo> _songs = new ArrayList<SongInfo>();;
    RecyclerView recyclerView;
    SeekBar seekBar;
    SongAdapter songAdapter;

    // Change this to use sound pool, to avoid loop gap
    // https://medium.com/sketchware/difference-between-soundpool-and-mediaplayer-bb79cda8bafc
    //https://examples.javacodegeeks.com/android/android-soundpool-example/
    MediaPlayer mediaPlayer;
    private Handler myHandler = new Handler();;
    private int lastPlayedPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //seekBar = (SeekBar) findViewById(R.id.seekBar);
        songAdapter = new SongAdapter(this,_songs);
        recyclerView.setAdapter(songAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Button b, View view, final SongInfo obj, final int position) {

                if(b.getText().equals("Stop")){
                    if(mediaPlayer!= null) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }
                    mediaPlayer = null;
                    b.setText("Play");
                } else {
                    // TODO Stop other songs
                    if(mediaPlayer!= null) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();

                        // This is hard coded ordering from the row_songs.xml. Any change there will break this
                        // TODO make it generic
                        View viewLast = recyclerView.getChildAt(lastPlayedPosition);
                        // This will be card view view
                        CardView cardView = (CardView) viewLast;
                        LinearLayout ll1 = (LinearLayout) cardView.getChildAt(0);
                        Button b2 = (Button)ll1.getChildAt(1);
                        b2.setText("Play");
                    }


                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                lastPlayedPosition = position;
                                mediaPlayer = new MediaPlayer();
                                if(obj.getSongUrl()!= null) {
                                    mediaPlayer.setDataSource(obj.getSongUrl());
                                } else {
                                    mediaPlayer.setDataSource(obj.getSongUrlFd());
                                }
                                mediaPlayer.prepareAsync();
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.setLooping(true);
                                        mp.start();
                                        /*seekBar.setProgress(0);
                                        seekBar.setMax(mediaPlayer.getDuration());*/
                                        Log.d("Prog", "run: " + mediaPlayer.getDuration());
                                    }
                                });
                                b.setText("Stop");



                            }catch (Exception e){}
                        }

                    };
                    myHandler.postDelayed(runnable,100);

                }
            }
        });
        checkUserPermission();
        Thread t = new runThread();
        //t.start();
    }

    // Functionality for the seek bar - not needed.
    public class runThread extends Thread {


        @Override
        public void run() {
            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("Runwa", "run: " + 1);
                if (mediaPlayer != null) {
                    seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    });

                    Log.d("Runwa", "run: " + mediaPlayer.getCurrentPosition());
                }
            }
        }

    }

    private void checkUserPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
                return;
            }
        }
        loadSongs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadSongs();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }

    /**
     * Generic method to load all mp3 songs on the device. We need to restrict it to only from
     * a certain directory
     */
    private void loadSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC ;//+"!=0";
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    SongInfo s = new SongInfo(name,artist,url);
                    // TODO Fix this to load only from a given directory. For now, discarding all device mp3
                    //_songs.add(s);

                }while (cursor.moveToNext());
            }

            cursor.close();
            // Add the songs from app internal storage
            try {
                // TODO: change to scan the asset directory
                AssetFileDescriptor afd = getAssets().openFd("WhiteNoise.mp3");
                SongInfo s = new SongInfo("White Noise", "Random", afd);
                _songs.add(s);
                afd = getAssets().openFd("RiverFlowing.mp3");
                s = new SongInfo("Flowing River", "Random", afd);
                _songs.add(s);
            } catch (Exception ex) {
                // do nothing
                System.out.println("test");
            }
            songAdapter = new SongAdapter(MainActivity.this,_songs);

        }
    }

}