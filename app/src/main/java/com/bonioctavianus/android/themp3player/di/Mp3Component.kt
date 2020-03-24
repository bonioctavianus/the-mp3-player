package com.bonioctavianus.android.themp3player.di

import com.bonioctavianus.android.themp3player.Mp3App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ServiceModule::class,
        FragmentModule::class,
        Mp3Module::class
    ]
)
interface Mp3Component {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Mp3App): Mp3Component
    }

    fun inject(application: Mp3App)

}