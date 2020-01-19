package net.hunnor.dict.android.activity.database;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DatabaseViewModel extends ViewModel {

    private MutableLiveData<Long> activeDownloadId;

    private MutableLiveData<String> progressReport;

    private MutableLiveData<String> remoteDate;

    private MutableLiveData<String> remoteSize;

    private MutableLiveData<String> remoteStatus;

    private MutableLiveData<String> localDate;

    private MutableLiveData<String> localSize;

    private MutableLiveData<String> localStatus;

    public MutableLiveData<Long> getActiveDownloadId() {
        if (activeDownloadId == null) {
            activeDownloadId = new MutableLiveData<>();
        }
        return activeDownloadId;
    }

    public void setActiveDownloadId(Long id) {
        if (activeDownloadId == null) {
            activeDownloadId = new MutableLiveData<>();
        }
        activeDownloadId.setValue(id);
    }

    public MutableLiveData<String> getProgressReport() {
        if (progressReport == null) {
            progressReport = new MutableLiveData<>();
        }
        return progressReport;
    }

    public void setProgressReport(String report) {
        if (progressReport == null) {
            progressReport = new MutableLiveData<>();
        }
        progressReport.setValue(report);
    }

    public LiveData<String> getRemoteDate() {
        if (remoteDate == null) {
            remoteDate = new MutableLiveData<>();
        }
        return remoteDate;
    }

    public void setRemoteDate(String date) {
        if (remoteDate == null) {
            remoteDate = new MutableLiveData<>();
        }
        remoteDate.setValue(date);
    }

    public LiveData<String> getRemoteSize() {
        if (remoteSize == null) {
            remoteSize = new MutableLiveData<>();
        }
        return remoteSize;
    }

    public void setRemoteSize(String size) {
        if (remoteSize == null) {
            remoteSize = new MutableLiveData<>();
        }
        remoteSize.setValue(size);
    }

    public LiveData<String> getRemoteStatus() {
        if (remoteStatus == null) {
            remoteStatus = new MutableLiveData<>();
        }
        return remoteStatus;
    }

    public void setRemoteStatus(String status) {
        if (remoteStatus == null) {
            remoteStatus = new MutableLiveData<>();
        }
        remoteStatus.setValue(status);
    }

    public LiveData<String> getLocalDate() {
        if (localDate == null) {
            localDate = new MutableLiveData<>();
        }
        return localDate;
    }

    public void setLocalDate(String date) {
        if (localDate == null) {
            localDate = new MutableLiveData<>();
        }
        localDate.setValue(date);
    }

    public LiveData<String> getLocalSize() {
        if (localSize == null) {
            localSize = new MutableLiveData<>();
        }
        return localSize;
    }

    public void setLocalSize(String size) {
        if (localSize == null) {
            localSize = new MutableLiveData<>();
        }
        localSize.setValue(size);
    }

    public LiveData<String> getLocalStatus() {
        if (localStatus == null) {
            localStatus = new MutableLiveData<>();
        }
        return localStatus;
    }

    public void setLocalStatus(String status) {
        if (localStatus == null) {
            localStatus = new MutableLiveData<>();
        }
        localStatus.setValue(status);
    }

}
