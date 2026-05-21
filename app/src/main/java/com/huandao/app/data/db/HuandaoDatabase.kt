package com.huandao.app.data.db

import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.huandao.app.data.db.converter.Converters
import com.huandao.app.data.db.dao.CoolPoolItemDao
import com.huandao.app.data.db.dao.EmotionTagDao
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.db.entity.CoolPoolItemTagCrossRef
import com.huandao.app.data.db.entity.EmotionTagEntity
import java.util.UUID

/**
 * 缓岛 Room 数据库定义。
 *
 * V1 策略：
 * - exportSchema = true，schema 文件输出到 app/schemas/
 * - fallbackToDestructiveMigration()（开发期，V2 开始写 Migration）
 * - 数据库创建时预填充 8 个系统预设标签
 */
@Database(
    entities = [
        CoolPoolItemEntity::class,
        EmotionTagEntity::class,
        CoolPoolItemTagCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class HuandaoDatabase : RoomDatabase() {

    abstract fun coolPoolItemDao(): CoolPoolItemDao
    abstract fun emotionTagDao(): EmotionTagDao

    companion object {
        const val DATABASE_NAME = "huandao.db"

        /** 8 个预设标签定义 */
        val PRESET_TAGS = listOf(
            Triple("tag_preset_stress", "压力大", "😫"),
            Triple("tag_preset_influenced", "被种草了", "🌱"),
            Triple("tag_preset_reward", "奖励自己", "🎁"),
            Triple("tag_preset_compare", "社交比较", "👀"),
            Triple("tag_preset_browse", "习惯性浏览", "📱"),
            Triple("tag_preset_bored", "无聊", "😴"),
            Triple("tag_preset_happy", "开心", "😊"),
            Triple("tag_preset_other", "其他", "🤔"),
        )

        /**
         * 数据库创建回调：预填充 8 个系统预设标签。
         */
        val CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                PRESET_TAGS.forEach { (id, name, emoji) ->
                    db.execSQL(
                        """
                        INSERT OR REPLACE INTO emotion_tags 
                            (id, name, emoji, is_predefined, color_hex, created_at) 
                        VALUES (?, ?, ?, 1, '#E8913A', ?)
                        """.trimIndent(),
                        arrayOf(id, name, emoji, now)
                    )
                }
            }
        }
    }
}
