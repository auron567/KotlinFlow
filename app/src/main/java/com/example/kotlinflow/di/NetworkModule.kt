package com.example.kotlinflow.di

import com.example.kotlinflow.data.network.EpisodeRemoteDataSource
import com.example.kotlinflow.data.network.EpisodeService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://raw.githubusercontent.com/"

val networkModule = module {

    // HttpLoggingInterceptor instance
    single { provideLoggingInterceptor() }
    // OkHttpClient instance
    single { provideOkHttpClient(get()) }
    // Retrofit instance
    single { provideRetrofit(get()) }
    // EpisodeService instance
    single { provideEpisodeService(get()) }
    // EpisodeRemoteDataSource instance
    single { provideEpisodeRemoteDataSource(get()) }
}

private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
}

private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideEpisodeService(retrofit: Retrofit): EpisodeService {
    return retrofit.create(EpisodeService::class.java)
}

private fun provideEpisodeRemoteDataSource(service: EpisodeService): EpisodeRemoteDataSource {
    return EpisodeRemoteDataSource(service)
}