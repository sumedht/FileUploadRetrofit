package com.sumedh.fileuploadretrofit

interface Downloader {
    fun downloadFile(url: String): Long
}