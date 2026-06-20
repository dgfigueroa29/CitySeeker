package com.boa.test.city.seeker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 =
    Migration(2, 3) { db: SupportSQLiteDatabase ->
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `journal_entries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `city_id` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `rating` INTEGER NOT NULL,
                `visit_date` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                FOREIGN KEY(`city_id`) REFERENCES `cities`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_entries_city_id` ON `journal_entries` (`city_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_entries_visit_date` ON `journal_entries` (`visit_date`)")
    }
