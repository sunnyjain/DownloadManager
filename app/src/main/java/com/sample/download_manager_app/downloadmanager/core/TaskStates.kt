package com.sample.download_manager_app.downloadmanager.core

object TaskStates {
   const val INIT        = 0
   const val READY       = 1
   const val DOWNLOADING = 2
   const val PAUSED      = 3
   const val DOWNLOAD_FINISHED = 4
   const val END         = 5

   
   const val PAUSING = 6
   const val CANCEL = 7
   const val RESUMING = 8
   const val NETWORK_FAILURE_PAUSE = 9
}