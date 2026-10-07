package com.example.data.local

import com.example.data.model.CommentEntity
import com.example.data.model.EpisodeEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.UserProfileEntity

object InitialData {

    val sampleProfile = UserProfileEntity(
        id = "dipzon_user_1",
        username = "Deniz Sinemasever",
        email = "deniz@dipzon.com",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80",
        preferredGenres = "Gerilim,Bilim Kurgu,Aksiyon,Dram",
        isPremium = true,
        premiumTier = "Dipzon VIP Ultra",
        coins = 150,
        videoQuality = "1080p FHD (Otomatik)",
        dataSaver = false,
        subtitleLanguage = "Türkçe",
        notificationsEnabled = true
    )

    val sampleSeries = listOf(
        SeriesEntity(
            id = "ser_karanlik_safak",
            title = "Karanlık Şafak",
            description = "İstanbul Boğazı'nda lüks bir yatta gerçekleşen esrarengiz cinayet. Olay mahallinden kaçan tek tanık ve şafağa kadar gerçeği çözmek zorunda olan cinayet masası dedektifi.",
            category = "Gerilim",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            year = 2025,
            ageRating = "16+",
            totalEpisodes = 6,
            totalSeasons = 1,
            director = "Alper Çağlar",
            cast = "Kerem Bürsin, Farah Zeynep Abdullah, Haluk Bilginer",
            matchRate = 99,
            viewsCount = 345000L,
            likesCount = 42100L,
            isTrending = true,
            isNew = true,
            isEditorChoice = true
        ),
        SeriesEntity(
            id = "ser_paralel_baglanti",
            title = "Paralel Bağlantı",
            description = "Gece vardiyasında çalışan bir kurye, adresi bulunamayan gizemli bir paketi açar. İçindeki telefona 120 dakika sonra yaşanacak ölümcül kazaların bildirimleri gelmektedir.",
            category = "Bilim Kurgu",
            posterUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80",
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            year = 2025,
            ageRating = "16+",
            totalEpisodes = 5,
            totalSeasons = 1,
            director = "Can Evrenol",
            cast = "Boran Kuzum, Miray Daner, Fırat Tanış",
            matchRate = 96,
            viewsCount = 289000L,
            likesCount = 38400L,
            isTrending = true,
            isNew = true,
            isEditorChoice = true
        ),
        SeriesEntity(
            id = "ser_kirmizi_kod",
            title = "Kırmızı Kod: Asayiş",
            description = "Özel Harekât timinden ihraç edilen tecrübeli yüzbaşı, kaçırılan kardeşini bulmak için yeraltı suç örgütüne tek başına sızar. Her bölüm zamana karşı ölümcül bir operasyon.",
            category = "Aksiyon",
            posterUrl = "https://images.unsplash.com/photo-1533488765986-dfa2a9939acd?w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=800&q=80",
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            year = 2024,
            ageRating = "18+",
            totalEpisodes = 6,
            totalSeasons = 1,
            director = "Tolga Karaçelik",
            cast = "Çağatay Ulusoy, Aras Bulut İynemli, Damla Sönmez",
            matchRate = 97,
            viewsCount = 412000L,
            likesCount = 56000L,
            isTrending = true,
            isNew = false,
            isEditorChoice = false
        ),
        SeriesEntity(
            id = "ser_son_randevu",
            title = "Son Randevu",
            description = "Karaköy vapurunda tesadüfen tanışan ve sadece bir gece sürecek bir anlaşma yapan iki yabancı. Geçmişin sırları ortaya çıktıkça bu gece bir daha asla tekrarlanamayacaktır.",
            category = "Romantik",
            posterUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&q=80",
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            year = 2025,
            ageRating = "13+",
            totalEpisodes = 5,
            totalSeasons = 1,
            director = "Hilal Saral",
            cast = "Serenay Sarıkaya, Kıvanç Tatlıtuğ",
            matchRate = 94,
            viewsCount = 210000L,
            likesCount = 31200L,
            isTrending = false,
            isNew = true,
            isEditorChoice = false
        ),
        SeriesEntity(
            id = "ser_golgeler_diyari",
            title = "Gölgeler Diyarı",
            description = "Kapalıçarşı'nın bin yıllık yer altı sarnıçlarında mühürlenmiş kadim bir kapı açılır. İstanbul sokaklarında insanlar arasında yaşayan gölge muhafızları uyanır.",
            category = "Fantastik",
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80",
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            year = 2024,
            ageRating = "16+",
            totalEpisodes = 5,
            totalSeasons = 1,
            director = "Onur Saylak",
            cast = "Mert Ramazan Demir, Afra Saraçoğlu, Selçuk Yöntem",
            matchRate = 92,
            viewsCount = 195000L,
            likesCount = 24100L,
            isTrending = false,
            isNew = false,
            isEditorChoice = false
        ),
        SeriesEntity(
            id = "ser_yalanin_bedeli",
            title = "Yalanın Bedeli",
            description = "Holding veliahtının şüpheli intiharının ardından, aile servetini ele geçirmek isteyen üvey anne ile gerçek mirasçı arasındaki entrika ve güç savaşı.",
            category = "Dram",
            posterUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800&q=80",
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            year = 2024,
            ageRating = "16+",
            totalEpisodes = 6,
            totalSeasons = 1,
            director = "Uluç Bayraktar",
            cast = "Ezel Akay, Cansu Dere, Barış Arduç",
            matchRate = 95,
            viewsCount = 310000L,
            likesCount = 44000L,
            isTrending = true,
            isNew = false,
            isEditorChoice = true
        )
    )

    val sampleEpisodes = listOf(
        // Karanlık Şafak
        EpisodeEntity(
            id = "ser_karanlik_safak_s1_e1",
            seriesId = "ser_karanlik_safak",
            seasonNumber = 1,
            episodeNumber = 1,
            title = "Gece Yarısı Çağrısı",
            durationSeconds = 154,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80",
            synopsis = "Boğaz'ın ortasında demirli yattan gelen acil yardım sinyali liman polisini alarma geçirir.",
            isFree = true
        ),
        EpisodeEntity(
            id = "ser_karanlik_safak_s1_e2",
            seriesId = "ser_karanlik_safak",
            seasonNumber = 1,
            episodeNumber = 2,
            title = "Kayıp Kamera",
            durationSeconds = 184,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80",
            synopsis = "Güvenlik kayıtlarının silindiği anlaşılır. Ancak kamarada unutulmuş bir cep telefonu her şeyi değiştirecektir.",
            isFree = true
        ),
        EpisodeEntity(
            id = "ser_karanlik_safak_s1_e3",
            seriesId = "ser_karanlik_safak",
            seasonNumber = 1,
            episodeNumber = 3,
            title = "Son Tanık",
            durationSeconds = 168,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=400&q=80",
            synopsis = "Olay gecesi yatta garsonluk yapan Gizem, Beyoğlu'nun ara sokaklarında izini kaybettirmeye çalışır.",
            isFree = true
        ),
        EpisodeEntity(
            id = "ser_karanlik_safak_s1_e4",
            seriesId = "ser_karanlik_safak",
            seasonNumber = 1,
            episodeNumber = 4,
            title = "Geri Sayım",
            durationSeconds = 210,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1533488765986-dfa2a9939acd?w=400&q=80",
            synopsis = "Dedektif Kemal'e bilinmeyen bir numaradan tehdit mesajı gelir: 'Dosyayı kapat yoksa ailen yanar.'",
            isFree = false
        ),
        EpisodeEntity(
            id = "ser_karanlik_safak_s1_e5",
            seriesId = "ser_karanlik_safak",
            seasonNumber = 1,
            episodeNumber = 5,
            title = "Köstebek",
            durationSeconds = 175,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&q=80",
            synopsis = "Emniyet içinde bilgi sızdıranın en yakın çalışma arkadaşı olduğunu fark eden Kemal tek başına harekete geçer.",
            isFree = false
        ),
        EpisodeEntity(
            id = "ser_karanlik_safak_s1_e6",
            seriesId = "ser_karanlik_safak",
            seasonNumber = 1,
            episodeNumber = 6,
            title = "Şafak Baskını",
            durationSeconds = 220,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400&q=80",
            synopsis = "Haliç tersanesinde gerçekleşen nefes kesen final hesaplaşması.",
            isFree = false
        ),

        // Paralel Bağlantı
        EpisodeEntity(
            id = "ser_paralel_baglanti_s1_e1",
            seriesId = "ser_paralel_baglanti",
            seasonNumber = 1,
            episodeNumber = 1,
            title = "Bilinmeyen Gönderici",
            durationSeconds = 160,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&q=80",
            synopsis = "Motosikletli kurye Can, gece 03:00'te isimsiz bir paketi teslim almak üzere terk edilmiş bir fabrikaya gider.",
            isFree = true
        ),
        EpisodeEntity(
            id = "ser_paralel_baglanti_s1_e2",
            seriesId = "ser_paralel_baglanti",
            seasonNumber = 1,
            episodeNumber = 2,
            title = "İlk Bildirim",
            durationSeconds = 190,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=400&q=80",
            synopsis = "Telefona düşen bildirim: '14:22 - Beşiktaş Meydanı'nda sarı taksi otobüs durağına çarpacak.' Can oraya koşar.",
            isFree = true
        ),
        EpisodeEntity(
            id = "ser_paralel_baglanti_s1_e3",
            seriesId = "ser_paralel_baglanti",
            seasonNumber = 1,
            episodeNumber = 3,
            title = "Zamanın İpucu",
            durationSeconds = 170,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80",
            synopsis = "Olay gerçekleştiğinde Can telefonun gerçekten geleceği gösterdiğini dehşetle anlar.",
            isFree = true
        ),

        // Kırmızı Kod
        EpisodeEntity(
            id = "ser_kirmizi_kod_s1_e1",
            seriesId = "ser_kirmizi_kod",
            seasonNumber = 1,
            episodeNumber = 1,
            title = "Sokak Kuralı",
            durationSeconds = 140,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1533488765986-dfa2a9939acd?w=400&q=80",
            synopsis = "Tarlabaşı'nda eski bir depoya yapılan ani baskınla tehlikeli bir intikam zinciri başlar.",
            isFree = true
        ),
        EpisodeEntity(
            id = "ser_kirmizi_kod_s1_e2",
            seriesId = "ser_kirmizi_kod",
            seasonNumber = 1,
            episodeNumber = 2,
            title = "Kovalamaca",
            durationSeconds = 165,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=400&q=80",
            synopsis = "Çatılarda geçen soluk soluğa bir takip.",
            isFree = true
        ),

        // Son Randevu
        EpisodeEntity(
            id = "ser_son_randevu_s1_e1",
            seriesId = "ser_son_randevu",
            seasonNumber = 1,
            episodeNumber = 1,
            title = "Karaköy Rüzgarı",
            durationSeconds = 150,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=400&q=80",
            synopsis = "Yağmurlu bir İstanbul akşamında iki yalnız kalbin beklenmedik karşılaşması.",
            isFree = true
        )
    )

    val sampleComments = listOf(
        CommentEntity(
            id = "com_1",
            seriesId = "ser_karanlik_safak",
            episodeId = "ser_karanlik_safak_s1_e1",
            userName = "Merve Kaya",
            userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&q=80",
            text = "Dikey formatta bu kadar sinematik gerilim beklemiyordum! Kamera açıları ve ışık kullanımı muazzam olmuş.",
            likesCount = 24,
            isSpoiler = false,
            timestampFormatted = "1s önce"
        ),
        CommentEntity(
            id = "com_2",
            seriesId = "ser_karanlik_safak",
            episodeId = "ser_karanlik_safak_s1_e1",
            userName = "Burak Yıldız",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&q=80",
            text = "Dedektifin yatın güvertesinde bulduğu anahtarlık aslında Gizem'in değil, 3. bölümdeki adamın!",
            likesCount = 45,
            isSpoiler = true,
            timestampFormatted = "3s önce"
        ),
        CommentEntity(
            id = "com_3",
            seriesId = "ser_karanlik_safak",
            episodeId = "ser_karanlik_safak_s1_e1",
            userName = "Selin Demir",
            userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&q=80",
            text = "2 dakikalık bölümler nasıl bu kadar sürükleyici olabiliyor? Diğer bölüme hemen geçtim!",
            likesCount = 18,
            isSpoiler = false,
            timestampFormatted = "5s önce"
        ),
        CommentEntity(
            id = "com_4",
            seriesId = "ser_paralel_baglanti",
            episodeId = "ser_paralel_baglanti_s1_e1",
            userName = "Emre Çelik",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&q=80",
            text = "Konsept Black Mirror havası veriyor, dikey format için mükemmel kurgulanmış.",
            likesCount = 37,
            isSpoiler = false,
            timestampFormatted = "4s önce"
        )
    )
}
