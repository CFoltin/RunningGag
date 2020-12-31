package eu.johannes.runninggag;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OnlyOneRunViewModel extends ViewModel {
    private final MutableLiveData<OnlyOneRun> selectedOnlyOneRun = new MutableLiveData<OnlyOneRun>();

    public void selectOnlyOneRun(OnlyOneRun onlyOneRun) {
        selectedOnlyOneRun.setValue(onlyOneRun);
    }

    public LiveData<OnlyOneRun> getSelectedOnlyOneRun() {
        return selectedOnlyOneRun;
    }
}