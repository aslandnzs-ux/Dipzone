package com.example.data.remote

import android.net.Uri
import com.example.data.local.DipzonDao
import com.example.data.model.EpisodeEntity
import com.example.data.model.SeriesEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Keeps the local Room catalogue in sync with the published Firebase catalogue.
 * If Firebase is not configured yet, the app continues to work locally.
 */
class FirebaseContentService(
    private val dao: DipzonDao,
    private val scope: CoroutineScope
) {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (_: Exception) { null }
    private var seriesListener: ListenerRegistration? = null
    private var episodeListener: ListenerRegistration? = null

    fun startRealtimeSync() {
        val db = firestore ?: return
        if (seriesListener != null) return

        seriesListener = db.collection("series")
            .whereEqualTo("isPublished", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val rows = snapshot.documents.mapNotNull { doc ->
                    try {
                        SeriesEntity(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            category = doc.getString("category").orEmpty(),
                            posterUrl = doc.getString("posterUrl").orEmpty(),
                            backdropUrl = doc.getString("backdropUrl").orEmpty(),
                            trailerUrl = doc.getString("trailerUrl").orEmpty(),
                            year = (doc.getLong("year") ?: 0L).toInt(),
                            ageRating = doc.getString("ageRating") ?: "Genel",
                            totalEpisodes = (doc.getLong("totalEpisodes") ?: 0L).toInt(),
                            totalSeasons = (doc.getLong("totalSeasons") ?: 1L).toInt(),
                            director = doc.getString("director").orEmpty(),
                            cast = doc.getString("cast").orEmpty(),
                            matchRate = (doc.getLong("matchRate") ?: 0L).toInt(),
                            viewsCount = doc.getLong("viewsCount") ?: 0L,
                            likesCount = doc.getLong("likesCount") ?: 0L,
                            isTrending = doc.getBoolean("isTrending") ?: false,
                            isNew = doc.getBoolean("isNew") ?: false,
                            isEditorChoice = doc.getBoolean("isEditorChoice") ?: false,
                            isPublished = doc.getBoolean("isPublished") ?: false
                        )
                    } catch (_: Exception) { null }
                }
                scope.launch(Dispatchers.IO) { rows.forEach { dao.upsertSeries(it) } }
            }

        episodeListener = db.collection("episodes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val rows = snapshot.documents.mapNotNull { doc ->
                    try {
                        EpisodeEntity(
                            id = doc.id,
                            seriesId = doc.getString("seriesId").orEmpty(),
                            seasonNumber = (doc.getLong("seasonNumber") ?: 1L).toInt(),
                            episodeNumber = (doc.getLong("episodeNumber") ?: 1L).toInt(),
                            title = doc.getString("title").orEmpty(),
                            durationSeconds = (doc.getLong("durationSeconds") ?: 0L).toInt(),
                            videoUrl = doc.getString("videoUrl").orEmpty(),
                            thumbnailUrl = doc.getString("thumbnailUrl").orEmpty(),
                            synopsis = doc.getString("synopsis").orEmpty(),
                            isFree = doc.getBoolean("isFree") ?: true,
                            publishedAt = doc.getLong("publishedAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) { null }
                }
                scope.launch(Dispatchers.IO) { rows.forEach { dao.upsertEpisode(it) } }
            }
    }

    fun publishSeries(series: SeriesEntity, onResult: (Result<Unit>) -> Unit = {}) {
        val db = firestore ?: return onResult(Result.failure(IllegalStateException("Firebase yapılandırılmamış")))
        db.collection("series").document(series.id).set(
            mapOf(
                "title" to series.title, "description" to series.description, "category" to series.category,
                "posterUrl" to series.posterUrl, "backdropUrl" to series.backdropUrl, "trailerUrl" to series.trailerUrl,
                "year" to series.year, "ageRating" to series.ageRating, "totalEpisodes" to series.totalEpisodes,
                "totalSeasons" to series.totalSeasons, "director" to series.director, "cast" to series.cast,
                "matchRate" to series.matchRate, "viewsCount" to series.viewsCount, "likesCount" to series.likesCount,
                "isTrending" to series.isTrending, "isNew" to series.isNew, "isEditorChoice" to series.isEditorChoice,
                "isPublished" to series.isPublished, "updatedAt" to System.currentTimeMillis()
            )
        ).addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun publishEpisode(episode: EpisodeEntity, onResult: (Result<Unit>) -> Unit = {}) {
        val db = firestore ?: return onResult(Result.failure(IllegalStateException("Firebase yapılandırılmamış")))
        db.collection("episodes").document(episode.id).set(
            mapOf(
                "seriesId" to episode.seriesId, "seasonNumber" to episode.seasonNumber,
                "episodeNumber" to episode.episodeNumber, "title" to episode.title,
                "durationSeconds" to episode.durationSeconds, "videoUrl" to episode.videoUrl,
                "thumbnailUrl" to episode.thumbnailUrl, "synopsis" to episode.synopsis,
                "isFree" to episode.isFree, "publishedAt" to episode.publishedAt
            )
        ).addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteSeries(seriesId: String) {
        firestore?.collection("series")?.document(seriesId)?.delete()
    }

    fun deleteEpisode(episodeId: String) {
        firestore?.collection("episodes")?.document(episodeId)?.delete()
    }

    fun uploadMedia(uri: Uri, remotePath: String, onProgress: (Int) -> Unit, onResult: (Result<String>) -> Unit) {
        val bucket = storage ?: return onResult(Result.failure(IllegalStateException("Firebase Storage yapılandırılmamış")))
        val ref = bucket.reference.child(remotePath)
        ref.putFile(uri)
            .addOnProgressListener { task ->
                val total = task.totalByteCount.coerceAtLeast(1L)
                onProgress(((task.bytesTransferred * 100L) / total).toInt())
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) throw (task.exception ?: IllegalStateException("Yükleme başarısız"))
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri -> onResult(Result.success(downloadUri.toString())) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
