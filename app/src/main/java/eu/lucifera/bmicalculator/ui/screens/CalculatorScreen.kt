package eu.lucifera.bmicalculator.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.lucifera.bmicalculator.R

@Composable
fun CalculatorScreen() {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text(stringResource(R.string.height_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text(stringResource(R.string.weight_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                val hCm = height.toDoubleOrNull()
                val wKg = weight.toDoubleOrNull()
                if (hCm != null && wKg != null && hCm > 0) {
                    val hMeter = hCm / 100.0
                    // BMI Formula: weight (kg) / height (m)^2
                    result = wKg / (hMeter * hMeter)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.calculate_btn))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        result?.let { bmi ->
            ResultDisplay(bmi)
        }
    }
}

@Composable
fun ResultDisplay(bmi: Double) {
    val categoryInfo = getCategoryInfo(bmi)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.result_label, bmi),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.category_label, stringResource(categoryInfo.labelRes)),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = categoryInfo.color,
            textAlign = TextAlign.Center
        )
    }
}

data class CategoryInfo(val labelRes: Int, val color: Color)

@Composable
fun getCategoryInfo(bmi: Double): CategoryInfo {
    return when {
        bmi < 18.5 -> CategoryInfo(R.string.cat_underweight, Color(0xFF03A9F4))
        bmi < 25.0 -> CategoryInfo(R.string.cat_healthy, Color(0xFF4CAF50))
        bmi < 30.0 -> CategoryInfo(R.string.cat_overweight, Color(0xFFFF9800))
        else -> CategoryInfo(R.string.cat_obese, Color(0xFFE91E63))
    }
}