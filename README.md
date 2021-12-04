# DownloadManager
DownloadManager is a service that handles long-running http downloads by conducting the process in the background. Clients can pause, resume/restart and stop/cancel download requests.

![](app_intro.gif)

# Tech Stack 

- Coroutines
- flow
- LiveData
- Room

# Lets get started

### Initialize

```kotlin
val downloadManager = DownloadManager(this)
```

### Usage

- Start download 

```kotlin
downloadManager.enqueue("https://your-download-link/download-file.pdf")
```

- Get download task list 
This method provides the task list liveData that can be observed and handle changes accordingly.

```kotlin
downloadManager.getTaskList().observe(this, Observer {
  //your code here.
})
```
- Actions

methods that allows the client to pause, resume/restart and stop/cancel the download.

```kotlin
downloadManager.pauseDownload(task)
downloadManager.resumeDownload(task)
downloadManager.stopDownload(task)
```
