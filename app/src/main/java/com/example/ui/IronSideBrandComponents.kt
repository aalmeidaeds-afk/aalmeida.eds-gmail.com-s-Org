package com.example.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.ui.theme.LocalClubIdentityConfig

@Composable
fun IronSideLogoBadge(
    size: Dp = 52.dp,
    modifier: Modifier = Modifier,
    overrideConfig: ConfiguracaoIdentidadeClube? = null
) {
    val config = overrideConfig ?: LocalClubIdentityConfig.current
    val shape = RoundedCornerShape(12.dp)

    if (config.logoUri.isNotBlank()) {
        AsyncImage(
            model = Uri.parse(config.logoUri),
            contentDescription = "Brasão Oficial ${config.nomeClube}",
            modifier = modifier
                .size(size)
                .clip(shape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, shape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118),
            placeholder = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118)
        )
    } else {
        when (config.logoPreset) {
            "escudo_cromado" -> {
                Box(
                    modifier = modifier
                        .size(size)
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    Color(0xFF1A1E24),
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                        .border(2.dp, MaterialTheme.colorScheme.secondary, shape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = config.nomeClube,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            "caveira_custom" -> {
                Box(
                    modifier = modifier
                        .size(size)
                        .clip(shape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    Color(0xFF0E1013)
                                )
                            )
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary, shape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = config.nomeClube,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size * 0.62f)
                    )
                }
            }
            "aguia_estrada" -> {
                Box(
                    modifier = modifier
                        .size(size)
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF141820),
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary, shape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = config.nomeClube,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size * 0.64f)
                    )
                }
            }
            "icone_moto_clube" -> {
                Box(
                    modifier = modifier
                        .size(size)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, shape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = config.nomeClube,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118),
                    contentDescription = "Brasão Oficial ${config.nomeClube} - ${config.lemaClube}",
                    modifier = modifier
                        .size(size)
                        .clip(shape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, shape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun IronSideFormHeader(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    val config = LocalClubIdentityConfig.current
    val resolvedSubtitle = subtitle.ifBlank {
        "${config.nomeClube} • ${config.lemaClube}"
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IronSideLogoBadge(size = 40.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = resolvedSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun MemberPhotoAvatar(
    fotoUri: String,
    nomeGuerra: String,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val isCustomUri = fotoUri.isNotBlank() && !fotoUri.startsWith("preset:")
    if (isCustomUri) {
        AsyncImage(
            model = Uri.parse(fotoUri),
            contentDescription = "Foto do Associado $nomeGuerra",
            modifier = modifier
                .size(size)
                .clip(shape)
                .border(2.dp, MaterialTheme.colorScheme.primary, shape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118),
            placeholder = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118)
        )
    } else {
        val bgColors = when (fotoUri) {
            "preset:comandante_estrada" -> listOf(Color(0xFF3E2723), Color(0xFF14181F))
            "preset:capitao_comboio" -> listOf(Color(0xFF1B3022), Color(0xFF101418))
            else -> listOf(Color(0xFF281D15), Color(0xFF14171D))
        }
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(Brush.linearGradient(bgColors))
                .border(2.dp, MaterialTheme.colorScheme.primary, shape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto do Membro $nomeGuerra",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size * 0.48f)
                )
                Text(
                    text = nomeGuerra.take(6).uppercase().ifBlank { "MEMBRO" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MotorcyclePhotoBanner(
    fotoUri: String,
    descricaoMoto: String,
    height: Dp = 140.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val isCustomUri = fotoUri.isNotBlank() && !fotoUri.startsWith("preset:")
    if (isCustomUri) {
        AsyncImage(
            model = Uri.parse(fotoUri),
            contentDescription = "Foto da Motocicleta $descricaoMoto",
            modifier = modifier
                .fillMaxWidth()
                .size(height = height, width = 260.dp)
                .clip(shape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), shape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.img_hero_motoclube_1791663228603),
            placeholder = painterResource(id = R.drawable.img_hero_motoclube_1791663228603)
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .size(height = height, width = 260.dp)
                .clip(shape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), shape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_motoclube_1791663228603),
                contentDescription = "Foto da Motocicleta $descricaoMoto",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x330E1013), Color(0xDD0E1013))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = descricaoMoto.ifBlank { "Motocicleta Oficial Cadastrada" },
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Foto Oficial da Motocicleta & Documento CRLV Vinculado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

