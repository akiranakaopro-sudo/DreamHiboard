package com.coui.appcompat.soundloadutil;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.provider.Settings;

import java.util.HashMap;

public class COUISoundLoadUtil {
    public static final int FLAG_BYPASS_MUTE = 128;
    private static COUISoundLoadUtil sInstance;

    private final HashMap<Integer, Integer> mSoundMap = new HashMap<>();
    private SoundPool mSoundPool;

    private COUISoundLoadUtil() {
        initSoundPool();
    }

    public static synchronized COUISoundLoadUtil getInstance() {
        if (sInstance == null) {
            sInstance = new COUISoundLoadUtil();
        }
        return sInstance;
    }

    private void initSoundPool() {
        SoundPool.Builder builder = new SoundPool.Builder();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setFlags(FLAG_BYPASS_MUTE)
                .setLegacyStreamType(1)
                .build();
        builder.setMaxStreams(10);
        builder.setAudioAttributes(audioAttributes);
        mSoundPool = builder.build();
    }

    private boolean querySoundEffectsEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "sound_effects_enabled", 0) != 0;
    }

    public int loadFile(String path, int priority) {
        return mSoundPool.load(path, priority);
    }

    public int loadSoundFile(Context context, int resId) {
        if (mSoundMap.containsKey(resId)) {
            return mSoundMap.get(resId);
        }
        int soundId = mSoundPool.load(context, resId, 0);
        mSoundMap.put(resId, soundId);
        return soundId;
    }

    public void play(Context context, int soundId, float leftVolume, float rightVolume, int priority, int loop, float rate) {
        if (querySoundEffectsEnabled(context)) {
            mSoundPool.play(soundId, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    public void setCompleteListener(SoundPool.OnLoadCompleteListener listener) {
        mSoundPool.setOnLoadCompleteListener(listener);
    }
}
