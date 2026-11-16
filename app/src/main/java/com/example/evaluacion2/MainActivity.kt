
package com.example.evaluacion2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.ui.theme.Evaluacion2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

data class Usuario(
    val nombre: String = "",
    val correo: String = ""
)

data class RegistroAcceso(
    val nombre: String = "",
    val correo: String = "",
    val tipo: String = "",
    val fecha: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Evaluacion2Theme {
                AppControlAcceso()
            }
        }
    }
}

// NAVEGACIÓN

@Composable
fun AppControlAcceso() {
    var pantalla by remember { mutableStateOf("login") }

    when (pantalla) {
        "login" -> LoginScreen(
            onLogin = { pantalla = "menu" }
        )

        "menu" -> MenuPrincipal(
            onAgregar = { pantalla = "agregar" },
            onVerUsuarios = { pantalla = "usuarios" },
            onRegistrarAcceso = { pantalla = "acceso" },
            onVerRegistros = { pantalla = "registros" },
            onCerrarSesion = {
                FirebaseAuth.getInstance().signOut()
                pantalla = "login"
            }
        )

        "agregar" -> AgregarUsuarioScreen(
            onVolver = { pantalla = "menu" }
        )

        "usuarios" -> ListaUsuariosScreen(
            onVolver = { pantalla = "menu" }
        )

        "acceso" -> RegistroAccesoScreen(
            onVolver = { pantalla = "menu" }
        )

        "registros" -> ListaRegistrosScreen(
            onVolver = { pantalla = "menu" }
        )
    }
}

// LOGIN

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "CONTROL DE ACCESO",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Identificación estudiantil")

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (correo.isBlank() || password.isBlank()) {
                    mensaje = "Completa todos los campos"
                } else {
                    cargando = true
                    mensaje = ""

                    auth.signInWithEmailAndPassword(
                        correo.trim(),
                        password
                    ).addOnCompleteListener { resultado ->
                        cargando = false

                        if (resultado.isSuccessful) {
                            onLogin()
                        } else {
                            mensaje =
                                "Error: ${resultado.exception?.localizedMessage}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text(if (cargando) "Ingresando..." else "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            mensaje,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// MENÚ PRINCIPAL

@Composable
fun MenuPrincipal(
    onAgregar: () -> Unit,
    onVerUsuarios: () -> Unit,
    onRegistrarAcceso: () -> Unit,
    onVerRegistros: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "MENÚ PRINCIPAL",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAgregar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar usuario")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onVerUsuarios,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver usuarios")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegistrarAcceso,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar entrada / salida")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onVerRegistros,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver registros de acceso")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// AGREGAR USUARIO

@Composable
fun AgregarUsuarioScreen(onVolver: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "AGREGAR USUARIO",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del estudiante") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nombre.isBlank() || correo.isBlank() || password.isBlank()) {
                    mensaje = "Completa todos los campos"
                } else if (password.length < 6) {
                    mensaje = "La contraseña debe tener al menos 6 caracteres"
                } else {
                    cargando = true
                    mensaje = ""

                    auth.createUserWithEmailAndPassword(
                        correo.trim(),
                        password
                    ).addOnCompleteListener { resultado ->

                        if (resultado.isSuccessful) {
                            val uid = auth.currentUser?.uid

                            if (uid != null) {
                                val usuario = hashMapOf(
                                    "nombre" to nombre.trim(),
                                    "correo" to correo.trim()
                                )

                                db.collection("usuarios")
                                    .document(uid)
                                    .set(usuario)
                                    .addOnSuccessListener {
                                        auth.signOut()
                                        mensaje = "Usuario registrado correctamente"
                                        cargando = false
                                        onVolver()
                                    }
                                    .addOnFailureListener { error ->
                                        mensaje =
                                            "Error al guardar: ${error.localizedMessage}"
                                        cargando = false
                                    }
                            } else {
                                mensaje = "No se pudo obtener el usuario"
                                cargando = false
                            }
                        } else {
                            mensaje =
                                "Error: ${resultado.exception?.localizedMessage}"
                            cargando = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text(if (cargando) "Guardando..." else "Guardar usuario")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            mensaje,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onVolver) {
            Text("Volver")
        }
    }
}

// LISTA DE USUARIOS

@Composable
fun ListaUsuariosScreen(onVolver: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }

    var usuarios by remember {
        mutableStateOf<List<Usuario>>(emptyList())
    }

    var mensaje by remember {
        mutableStateOf("Cargando usuarios...")
    }

    DisposableEffect(Unit) {
        val listener = db.collection("usuarios")
            .addSnapshotListener { resultado, error ->

                if (error != null) {
                    mensaje = "Error al cargar usuarios: ${error.localizedMessage}"
                } else if (resultado != null) {
                    usuarios = resultado.documents.mapNotNull {
                        it.toObject(Usuario::class.java)
                    }

                    mensaje = if (usuarios.isEmpty()) {
                        "No hay usuarios registrados"
                    } else {
                        ""
                    }
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "USUARIOS REGISTRADOS",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(usuarios) { usuario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Nombre: ${usuario.nombre}")
                        Text("Correo: ${usuario.correo}")
                    }
                }
            }
        }

        Text(mensaje)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}

// REGISTRAR ENTRADA Y SALIDA

@Composable
fun RegistroAccesoScreen(onVolver: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("Cargando usuario...") }
    var cargando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            mensaje = "No hay una sesión iniciada"
        } else {
            correo = usuarioActual.email ?: ""

            db.collection("usuarios")
                .document(usuarioActual.uid)
                .get()
                .addOnSuccessListener { documento ->
                    nombre = documento.getString("nombre") ?: ""
                    mensaje = if (nombre.isNotBlank()) {
                        "Usuario identificado correctamente"
                    } else {
                        "No se encontró el nombre. Se usará el correo."
                    }
                }
                .addOnFailureListener {
                    nombre = ""
                    mensaje = "No se pudo cargar el nombre. Se usará el correo."
                }
        }
    }

    fun registrar(tipo: String) {
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            mensaje = "Debes iniciar sesión primero"
            return
        }

        if (cargando) return

        cargando = true
        mensaje = "Registrando..."

        val registro = hashMapOf(
            "uid" to usuarioActual.uid,
            "nombre" to nombre.ifBlank { "Usuario" },
            "correo" to (usuarioActual.email ?: ""),
            "tipo" to tipo,
            "fecha" to FieldValue.serverTimestamp()
        )

        db.collection("registros_acceso")
            .add(registro)
            .addOnSuccessListener {
                mensaje = "$tipo registrado correctamente"
                cargando = false
            }
            .addOnFailureListener { error ->
                mensaje = "No se pudo guardar: ${error.localizedMessage}"
                cargando = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "REGISTRO DE ACCESO",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (nombre.isNotBlank() && nombre != correo) {
            Text("Estudiante: $nombre")
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text("Correo: $correo")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { registrar("ENTRADA") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando && auth.currentUser != null
        ) {
            Text("Registrar entrada")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { registrar("SALIDA") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando && auth.currentUser != null
        ) {
            Text("Registrar salida")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(mensaje)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(onClick = onVolver) {
            Text("Volver al menú")
        }
    }
}

// VER REGISTROS EN TIEMPO REAL

@Composable
fun ListaRegistrosScreen(onVolver: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }

    var registros by remember {
        mutableStateOf<List<RegistroAcceso>>(emptyList())
    }

    var mensaje by remember {
        mutableStateOf("Cargando registros...")
    }

    DisposableEffect(Unit) {
        val listener = db.collection("registros_acceso")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { resultado, error ->

                if (error != null) {
                    mensaje =
                        "Error al cargar registros: ${error.localizedMessage}"
                } else if (resultado != null) {

                    registros = resultado.documents.map { documento ->

                        val timestamp = documento.getTimestamp("fecha")

                        val fechaTexto = timestamp?.let {
                            SimpleDateFormat(
                                "dd/MM/yyyy HH:mm:ss",
                                Locale.getDefault()
                            ).format(it.toDate())
                        } ?: "Procesando fecha..."

                        RegistroAcceso(
                            nombre = documento.getString("nombre") ?: "",
                            correo = documento.getString("correo") ?: "",
                            tipo = documento.getString("tipo") ?: "",
                            fecha = fechaTexto
                        )
                    }

                    mensaje = if (registros.isEmpty()) {
                        "Todavía no hay registros de acceso"
                    } else {
                        ""
                    }
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "REGISTROS DE ACCESO",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(registros) { registro ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Movimiento: ${registro.tipo}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Nombre: ${registro.nombre}")
                        Text("Correo: ${registro.correo}")
                        Text("Fecha: ${registro.fecha}")
                    }
                }
            }
        }

        Text(mensaje)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}
