package com.himz.soundbaby;

import android.content.res.AssetFileDescriptor;

public class SongInfo {
    private String Songname;
    private String Artistname;
    private String SongUrl;
    private AssetFileDescriptor songUrlFd;

    public void setSongUrlFd(AssetFileDescriptor songUrlFd) {
        this.songUrlFd = songUrlFd;
    }

    public AssetFileDescriptor getSongUrlFd() {
        return songUrlFd;
    }

    public SongInfo() {
    }

    public SongInfo(String songname, String artistname, AssetFileDescriptor songUrlFd) {
        Songname = songname;
        Artistname = artistname;
        this.SongUrl = null;
        this.songUrlFd = songUrlFd;
    }

    public SongInfo(String songname, String artistname, String songUrl) {
        Songname = songname;
        Artistname = artistname;
        SongUrl = songUrl;
        this.songUrlFd = null;
    }

    public String getSongname() {
        return Songname;
    }

    public void setSongname(String songname) {
        Songname = songname;
    }

    public String getArtistname() {
        return Artistname;
    }

    public void setArtistname(String artistname) {
        Artistname = artistname;
    }

    public String getSongUrl() {
        return SongUrl;
    }

    public void setSongUrl(String songUrl) {
        SongUrl = songUrl;
    }
}
