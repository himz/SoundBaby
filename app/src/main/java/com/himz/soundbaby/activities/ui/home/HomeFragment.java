package com.himz.soundbaby.activities.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.himz.soundbaby.R;
import com.himz.soundbaby.activities.MainActivity;
import com.himz.soundbaby.adapters.SongAdapter;
import com.himz.soundbaby.adapters.SongInfo;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        //seekBar = (SeekBar) findViewById(R.id.seekBar);
        songAdapter = new SongAdapter(getActivity(),_songs);
        recyclerView.setAdapter(songAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
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

        return root;
    }

    private void checkUserPermission(){
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK},124);
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
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
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }


    @Override
    public void onStop() {
        super.onStop();

        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
    /**
     * Generic method to load all mp3 songs on the device. We need to restrict it to only from
     * a certain directory
     */
    private void loadSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC ;//+"!=0";
        Cursor cursor = getActivity().getContentResolver().query(uri,null,selection,null,null);
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
                AssetFileDescriptor afd = getActivity().getAssets().openFd("WhiteNoise.mp3");
                SongInfo s = new SongInfo("White Noise", "Random", afd);
                _songs.add(s);
                afd = getActivity().getAssets().openFd("RiverFlowing.mp3");
                s = new SongInfo("Flowing River", "Random", afd);
                _songs.add(s);
            } catch (Exception ex) {
                // do nothing
                System.out.println("test");
            }
            songAdapter = new SongAdapter(getActivity(),_songs);

        }
    }
}