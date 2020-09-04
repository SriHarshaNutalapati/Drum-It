package com.elevenstudio.bopittwistitpullit.utility;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;



public class sound_manager {

    private  SoundPool mSoundPool;
    private  HashMap<Integer, Integer> mSoundPoolMap;
    private  AudioManager  mAudioManager;
    private  Context mContext;
    public static final int maxSounds = 1;

    public sound_manager()
    {

    }

    public void initSounds(Context theContext) {
        mContext = theContext;
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundPoolMap = new HashMap<Integer, Integer>();
        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void addSound(int Index,int SoundID)
    {
        mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));

    }

    public int playSound(int index, Boolean sound_setting) {
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume=(float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume= streamVolume/maxVolume;
        return sound_setting?mSoundPool.play(mSoundPoolMap.get(index), volume, volume, 1, 0, 1f):0;
    }
}