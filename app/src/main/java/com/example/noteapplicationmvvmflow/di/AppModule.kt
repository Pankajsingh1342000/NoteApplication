package com.example.noteapplicationmvvmflow.di

import android.content.Context
import androidx.room.Room
import com.example.noteapplicationmvvmflow.data.db.NoteDao
import com.example.noteapplicationmvvmflow.data.db.NoteDataBase
import com.example.noteapplicationmvvmflow.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): NoteDataBase {
        return Room.databaseBuilder(
            context,
            NoteDataBase::class.java,
            "note_database"
        ).build()
    }

    @Provides
    fun provideNoteDao(db: NoteDataBase): NoteDao = db.noteDao()

    @Provides
    fun provideNoteRepository(dao: NoteDao): NoteRepository = NoteRepository(dao)
}