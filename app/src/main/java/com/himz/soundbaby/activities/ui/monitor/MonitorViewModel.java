package com.himz.soundbaby.activities.ui.monitor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MonitorViewModel extends ViewModel {

    public void setmText(MutableLiveData<String> mText) {
        this.mText = mText;
    }

    public void setmText(String mText) {
        this.mText.setValue(mText);
    }

    private MutableLiveData<String> mText;

    public MonitorViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is monitor fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}