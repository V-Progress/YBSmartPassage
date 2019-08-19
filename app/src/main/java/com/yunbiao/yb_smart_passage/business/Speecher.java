package com.yunbiao.yb_smart_passage.business;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import com.yunbiao.yb_smart_passage.APP;

public class Speecher {
    private static Speecher speecher;
    private static TextToSpeech mSpeech;
    private static boolean isInited = false;

    public synchronized static void init(Context context){
        if(speecher == null){
            speecher = new Speecher();
        }
        mSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                isInited = status == TextToSpeech.SUCCESS;
            }
        });
    }

    public static void speech(String content){
        if(!isInited){
            return;
        }
        if(mSpeech == null){
            init(APP.getContext());
        }

        if(mSpeech != null){
            mSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
