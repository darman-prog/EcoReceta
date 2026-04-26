package eco.receta.app.core.components



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorFieldBg   = Color(0x65D1C5AC)
private val ColorLabelText = Color(0xFF8A8A8A)

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorLabelText,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFBBBBBB), fontSize = 14.sp) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = errorMessage != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = ColorFieldBg,
                focusedContainerColor = ColorFieldBg,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFFD94F3D),
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = Color(0xFFFFF0EE)
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}