package com.huandao.app.di

import android.content.Context
import androidx.room.Room
import com.huandao.app.data.db.HuandaoDatabase
import com.huandao.app.data.db.dao.CoolPoolItemDao
import com.huandao.app.data.db.dao.EmotionTagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI 模块：提供 Room Database 单例及其 DAO 绑定。
 * Repository 层通过 @Inject constructor 自动获取 DAO 依赖。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HuandaoDatabase {
        return Room.databaseBuilder(
            context,
            HuandaoDatabase::class.java,
            HuandaoDatabase.DATABASE_NAME
        )
            .addCallback(HuandaoDatabase.CALLBACK)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCoolPoolItemDao(db: HuandaoDatabase): CoolPoolItemDao {
        return db.coolPoolItemDao()
    }

    @Provides
    fun provideEmotionTagDao(db: HuandaoDatabase): EmotionTagDao {
        return db.emotionTagDao()
    }
}
