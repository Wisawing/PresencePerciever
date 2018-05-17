package com.example.panut.presencereceiver;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;

import java.util.HashMap;

/**
 * Created by Panut on 09-May-18.
 */

public class AudioViewModel extends ViewModel {

    public class AudioData {
        public int id;
        public short buffer[];
    }

    private HashMap<Object, MutableLiveData<AudioData>> mAudioBuffers;

//    public LiveData<Queue<Short>> getAudioBuffer(){
//        if(mAudioBuffer == null)
//            mAudioBuffer = new MutableLiveData<Queue<Short>>();
//
//        return mAudioBuffer;
//    }

    // TODO this method is a bit odd. It currently both init and getter in the same method. refactoring recommended
    // make sure for this owner has already existed
    public LiveData<AudioData> initializeBuffer(Object owner){
        if(mAudioBuffers == null) {
            mAudioBuffers = new HashMap<>();
        }

        MutableLiveData<AudioData> buffer = mAudioBuffers.get(owner);

        if(buffer == null) {
            buffer = new MutableLiveData<>();
            mAudioBuffers.put(owner, buffer);
        }

        return buffer;
    }

    public LiveData<AudioData> getAudioBuffer(Object owner){

        LiveData<AudioData> buffer = initializeBuffer(owner);

        return buffer;
    }

    public void observeAllBuffer(LifecycleOwner observerOwner, Observer<AudioData> observer) {

    }


    public void setAudioBuffer(short[] audioData, Object owner){
        MutableLiveData<AudioData> liveBuffer = mAudioBuffers.get(owner);

        AudioData data = new AudioData();
        data.buffer = audioData;

        liveBuffer.postValue(data); // post data can be called outside of the main thread

        // TODO Check Data Continuity using live data might skip some value if the data is set within the same frame
    }

//    public short[] getAllAudioBuffer(int maxSize){
//        Queue<Short> dataQueue = mAudioBuffer.getValue();
//
//        int bufferSize;
//        if(dataQueue.size() < maxSize)
//            bufferSize = dataQueue.size();
//        else
//            bufferSize = maxSize;
//
//        return getAudioBuffer(bufferSize);
//    }

//    public void writeToAudioBuffer(short data[]){
//        if(mAudioBuffer == null) {
//            mAudioBuffer = new MutableLiveData<Queue<Short>>();
//            mAudioBuffer.setValue(new ArrayDeque<>());
//        }
//
//        for(int i = 0; i < data.length; i++)
//            mAudioBuffer.getValue().add(data[i]);
//    }
}
