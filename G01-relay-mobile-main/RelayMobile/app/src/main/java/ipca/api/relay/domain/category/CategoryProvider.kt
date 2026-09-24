package ipca.api.relay.domain.category

import ipca.api.relay.domain.model.Category
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


object CategoryProvider {

    // 🔍 Todas as categorias (Search Screen)

    val allCategories = listOf(
        Category("Books & Study", Icons.Default.MenuBook),        // Livros, Sebentas, Resumos
        Category("Electronics", Icons.Default.LaptopMac),         // Portáteis, Calculadoras, Ratos
        Category("Lab & Science", Icons.Default.Science),         // Batas, Pipetas, Microscópios
        Category("Art & Design", Icons.Default.Palette),          // Cavaletes, K-Line, Tintas
        Category("Architecture", Icons.Default.Architecture),     // Esquadros, Maquetes
        Category("Tools & Engineering", Icons.Default.Engineering), // Multímetros, Componentes, Ferramentas
        Category("Furniture", Icons.Default.Chair),               // Cadeiras de escritório, Candeeiros
        Category("Sports", Icons.Default.SportsBasketball),       // Equipamento de desporto, Raquetes
        Category("Fashion", Icons.Default.Checkroom),             // Tunas, Batas, Roupa de marca da uni
        Category("Music", Icons.Default.MusicNote),               // Instrumentos, Cabos
    )

    // 🏠 Home categories (chips)
    val homeCategories = listOf(
        "All",
        "Mathematics",
        "Biology",
        "Economics",
        "Law"
    )

    fun iconForCategory(name: String): ImageVector = when (name) {
        "Books & Study" -> Icons.Default.MenuBook
        "Electronics" -> Icons.Default.LaptopMac
        "Lab & Science" -> Icons.Default.Science
        "Art & Design" -> Icons.Default.Palette
        "Architecture" -> Icons.Default.Architecture
        "Tools & Engineering" -> Icons.Default.Engineering
        "Furniture" -> Icons.Default.Chair
        "Sports" -> Icons.Default.SportsBasketball
        "Fashion" -> Icons.Default.Checkroom
        "Music" -> Icons.Default.MusicNote
        else -> Icons.Default.MoreHoriz
    }
}