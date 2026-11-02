package com.example.navyugadmin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ---------------------------------------------------------
// NAVYUG THEME
// ---------------------------------------------------------

private val NavyugNavy = Color(0xFF213B52)
private val NavyugGold = Color(0xFFfdc134)
private val PageBackground = Color(0xFFF3F6FA)

// ---------------------------------------------------------
// API URLS
// ---------------------------------------------------------

private const val BASE_URL = "https://navyugdemo.cu.ma"

private const val LOGIN_URL = "$BASE_URL/api/login.php"
private const val DASHBOARD_URL = "$BASE_URL/api/dashboard.php"
private const val EVENTS_URL = "$BASE_URL/api/events.php"
private const val MAGAZINES_URL = "$BASE_URL/api/magazines.php"
private const val SEMINARS_URL = "$BASE_URL/api/seminars.php"
private const val GALLERY_URL = "$BASE_URL/api/gallery.php"

private const val CONTACT_MESSAGES_URL =
    "$BASE_URL/api/contact_messages.php"

private const val ADMISSIONS_URL =
    "$BASE_URL/api/admissions.php"

private const val CAREERS_URL =
    "$BASE_URL/api/careers.php"

// ---------------------------------------------------------
// MAIN ACTIVITY
// ---------------------------------------------------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                NavyugLoginScreen()
            }
        }
    }
}

// ---------------------------------------------------------
// DATA CLASSES
// ---------------------------------------------------------

private data class LoginResult(
    val success: Boolean,
    val message: String,
    val name: String = ""
)

private data class DashboardModule(
    val key: String,
    val label: String,
    val subtitle: String,
    val color: String
)

private data class DashboardResult(
    val success: Boolean,
    val message: String,
    val name: String = "",
    val username: String = "",
    val role: String = "",
    val modules: List<DashboardModule> = emptyList()
)

private data class EventRecord(
    val id: Int,
    val title: String,
    val eventDate: String,
    val description: String,
    val files: List<String>,
    val createdAt: String
)

private data class EventsResult(
    val success: Boolean,
    val message: String,
    val events: List<EventRecord> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

private data class MagazineRecord(
    val id: Int,
    val title: String,
    val issueDate: String,
    val description: String,
    val files: List<String>,
    val createdAt: String
)

private data class MagazinesResult(
    val success: Boolean,
    val message: String,
    val magazines: List<MagazineRecord> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

private data class SeminarRecord(
    val id: Int,
    val title: String,
    val seminarDate: String,
    val description: String,
    val imagePath: String,
    val createdAt: String
)

private data class SeminarsResult(
    val success: Boolean,
    val message: String,
    val seminars: List<SeminarRecord> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

private data class GalleryRecord(
    val id: Int,
    val title: String,
    val description: String,
    val imagePath: String,
    val createdAt: String
)

private data class GalleryResult(
    val success: Boolean,
    val message: String,
    val gallery: List<GalleryRecord> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

private data class ContactMessageRecord(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val subject: String,
    val message: String,
    val status: String,
    val createdAt: String,
    val readAt: String
)

private data class ContactMessagesResult(
    val success: Boolean,
    val message: String,
    val messages: List<ContactMessageRecord> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

// ADMISSIONS DATA

private data class AdmissionRecord(
    val id: Int,
    val applicationNo: String,
    val admissionType: String,
    val academicYear: String,
    val applyingFor: String,
    val studentName: String,
    val dob: String,
    val gender: String,
    val fatherName: String,
    val motherName: String,
    val guardianPhone: String,
    val guardianEmail: String,
    val address: String,
    val previousInstitution: String,
    val previousClass: String,
    val previousResult: String,
    val studentPhoto: String,
    val birthCertificate: String,
    val marksheet: String,
    val leavingCertificate: String,
    val status: String,
    val adminRemarks: String,
    val createdAt: String,
    val updatedAt: String
)

private data class AdmissionsResult(
    val success: Boolean,
    val message: String,
    val applications: List<AdmissionRecord> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

private data class CareerRecord(
    val id: Int,
    val applicationNo: String,
    val applicantName: String,
    val email: String,
    val phone: String,
    val positionApplied: String,
    val qualification: String,
    val experienceYears: String,
    val currentAddress: String,
    val coverLetter: String,
    val resumePath: String
)

private data class CareersResult(
    val success: Boolean,
    val message: String,
    val applications: List<CareerRecord> = emptyList()
)

// ---------------------------------------------------------
// PHP SESSION COOKIE MANAGER
// ---------------------------------------------------------

private fun getSessionCookieManager(): CookieManager {

    synchronized(CookieHandler::class.java) {

        val existing = CookieHandler.getDefault()

        if (existing is CookieManager) {
            return existing
        }

        val manager = CookieManager(
            null,
            CookiePolicy.ACCEPT_ALL
        )

        CookieHandler.setDefault(manager)

        return manager
    }
}

// ---------------------------------------------------------
// FILE URL HELPERS
// ---------------------------------------------------------

private fun getFileUrl(path: String): String {

    val cleanPath = path.trim()

    if (cleanPath.isBlank() || cleanPath == "null") return ""

    return if (
        cleanPath.startsWith("https://", ignoreCase = true) ||
        cleanPath.startsWith("http://", ignoreCase = true)
    ) {
        cleanPath
    } else {
        "$BASE_URL/admin/${cleanPath.trimStart('/')}"
    }
}

private fun getEventImageUrl(path: String): String =
    getFileUrl(path)

private fun getMagazinePdfUrl(path: String): String =
    getFileUrl(path)

private fun getSeminarImageUrl(path: String): String =
    getFileUrl(path)

private fun getGalleryImageUrl(path: String): String =
    getFileUrl(path)

private fun getAdmissionFileUrl(path: String): String =
    getFileUrl(path)

// ---------------------------------------------------------
// MAIN LOGIN AND DASHBOARD SCREEN
// ---------------------------------------------------------

@Composable
fun NavyugLoginScreen() {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var loginSucceeded by remember { mutableStateOf(false) }

    var loggedInName by remember { mutableStateOf("") }
    var loggedInUsername by remember { mutableStateOf("") }
    var loggedInRole by remember { mutableStateOf("") }

    var dashboardLoading by remember { mutableStateOf(false) }
    var dashboardLoaded by remember { mutableStateOf(false) }
    var dashboardMessage by remember { mutableStateOf("") }

    var modules by remember {
        mutableStateOf<List<DashboardModule>>(emptyList())
    }

    var selectedModule by remember {
        mutableStateOf<DashboardModule?>(null)
    }

    // EVENTS STATE

    var events by remember {
        mutableStateOf<List<EventRecord>>(emptyList())
    }

    var eventsLoading by remember { mutableStateOf(false) }
    var eventsMessage by remember { mutableStateOf("") }
    var eventsPage by remember { mutableIntStateOf(1) }
    var eventsTotalPages by remember { mutableIntStateOf(1) }

    // MAGAZINES STATE

    var magazines by remember {
        mutableStateOf<List<MagazineRecord>>(emptyList())
    }

    var magazinesLoading by remember { mutableStateOf(false) }
    var magazinesMessage by remember { mutableStateOf("") }
    var magazinesPage by remember { mutableIntStateOf(1) }
    var magazinesTotalPages by remember { mutableIntStateOf(1) }

    // SEMINARS STATE

    var seminars by remember {
        mutableStateOf<List<SeminarRecord>>(emptyList())
    }

    var seminarsLoading by remember { mutableStateOf(false) }
    var seminarsMessage by remember { mutableStateOf("") }
    var seminarsPage by remember { mutableIntStateOf(1) }
    var seminarsTotalPages by remember { mutableIntStateOf(1) }

    // GALLERY STATE

    var gallery by remember {
        mutableStateOf<List<GalleryRecord>>(emptyList())
    }

    var galleryLoading by remember { mutableStateOf(false) }
    var galleryMessage by remember { mutableStateOf("") }
    var galleryPage by remember { mutableIntStateOf(1) }
    var galleryTotalPages by remember { mutableIntStateOf(1) }

    // CONTACT MESSAGES STATE

    var contactMessages by remember {
        mutableStateOf<List<ContactMessageRecord>>(emptyList())
    }

    var contactMessagesLoading by remember {
        mutableStateOf(false)
    }

    var contactMessagesMessage by remember {
        mutableStateOf("")
    }

    var contactMessagesPage by remember {
        mutableIntStateOf(1)
    }

    var contactMessagesTotalPages by remember {
        mutableIntStateOf(1)
    }

    // ADMISSIONS STATE

    var admissions by remember {
        mutableStateOf<List<AdmissionRecord>>(emptyList())
    }

    var admissionsLoading by remember {
        mutableStateOf(false)
    }

    var admissionsMessage by remember {
        mutableStateOf("")
    }

    var admissionsPage by remember {
        mutableIntStateOf(1)
    }

    var admissionsTotalPages by remember {
        mutableIntStateOf(1)
    }

    var admissionSearch by remember {
        mutableStateOf("")
    }

    var admissionTypeFilter by remember {
        mutableStateOf("all")
    }

    var admissionStatusFilter by remember {
        mutableStateOf("all")
    }

    var selectedAdmission by remember {
        mutableStateOf<AdmissionRecord?>(null)
    }

    // CAREERS STATE

    var careerApplications by remember {
        mutableStateOf<List<CareerRecord>>(emptyList())
    }

    var careersLoading by remember { mutableStateOf(false) }
    var careersMessage by remember { mutableStateOf("") }

    val careerContext = LocalContext.current

    val scope = rememberCoroutineScope()

    // -----------------------------------------------------
    // LOAD EVENTS
    // -----------------------------------------------------

    fun loadEvents(page: Int) {
        scope.launch {
            eventsLoading = true
            eventsMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchEvents(page)
            }

            eventsLoading = false

            if (result.success) {
                events = result.events
                eventsPage = result.currentPage
                eventsTotalPages = result.totalPages
            } else {
                eventsMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // LOAD MAGAZINES
    // -----------------------------------------------------

    fun loadMagazines(page: Int) {
        scope.launch {
            magazinesLoading = true
            magazinesMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchMagazines(page)
            }

            magazinesLoading = false

            if (result.success) {
                magazines = result.magazines
                magazinesPage = result.currentPage
                magazinesTotalPages = result.totalPages
            } else {
                magazinesMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // LOAD SEMINARS
    // -----------------------------------------------------

    fun loadSeminars(page: Int) {
        scope.launch {
            seminarsLoading = true
            seminarsMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchSeminars(page)
            }

            seminarsLoading = false

            if (result.success) {
                seminars = result.seminars
                seminarsPage = result.currentPage
                seminarsTotalPages = result.totalPages
            } else {
                seminarsMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // LOAD GALLERY
    // -----------------------------------------------------

    fun loadGallery(page: Int) {
        scope.launch {
            galleryLoading = true
            galleryMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchGallery(page)
            }

            galleryLoading = false

            if (result.success) {
                gallery = result.gallery
                galleryPage = result.currentPage
                galleryTotalPages = result.totalPages
            } else {
                galleryMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // LOAD CONTACT MESSAGES
    // -----------------------------------------------------

    fun loadContactMessages(page: Int) {
        scope.launch {
            contactMessagesLoading = true
            contactMessagesMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchContactMessages(page)
            }

            contactMessagesLoading = false

            if (result.success) {
                contactMessages = result.messages
                contactMessagesPage = result.currentPage
                contactMessagesTotalPages = result.totalPages
            } else {
                contactMessagesMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // LOAD ADMISSIONS
    // -----------------------------------------------------

    fun loadAdmissions(
        page: Int = 1,
        search: String = admissionSearch,
        type: String = admissionTypeFilter,
        status: String = admissionStatusFilter
    ) {
        scope.launch {
            admissionsLoading = true
            admissionsMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchAdmissions(
                    page = page,
                    search = search,
                    admissionType = type,
                    status = status
                )
            }

            admissionsLoading = false

            if (result.success) {
                admissions = result.applications
                admissionsPage = result.currentPage
                admissionsTotalPages = result.totalPages
            } else {
                admissionsMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // LOAD CAREERS
    // -----------------------------------------------------

    fun loadCareers() {
        scope.launch {
            careersLoading = true
            careersMessage = ""

            val result = withContext(Dispatchers.IO) {
                fetchCareers()
            }

            careersLoading = false

            if (result.success) {
                careerApplications = result.applications
            } else {
                careersMessage = result.message
            }
        }
    }

    // -----------------------------------------------------
    // BACK BUTTON
    // -----------------------------------------------------

    BackHandler(
        enabled = loginSucceeded && selectedModule != null
    ) {
        if (selectedAdmission != null) {
            selectedAdmission = null
        } else {
            selectedModule = null

            events = emptyList()
            eventsMessage = ""

            magazines = emptyList()
            magazinesMessage = ""

            seminars = emptyList()
            seminarsMessage = ""

            gallery = emptyList()
            galleryMessage = ""

            contactMessages = emptyList()
            contactMessagesMessage = ""

            admissions = emptyList()
            admissionsMessage = ""

            careerApplications = emptyList()
            careersMessage = ""
            careersLoading = false

            admissionSearch = ""
            admissionTypeFilter = "all"
            admissionStatusFilter = "all"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (loginSucceeded && selectedModule == null) Color(0xFF080D18) else PageBackground
    ) {

        if (loginSucceeded && selectedModule == null) {
            DarkPremiumDashboard(
                name = loggedInName,
                username = loggedInUsername,
                role = loggedInRole,
                modules = modules,
                loading = dashboardLoading,
                loaded = dashboardLoaded,
                message = dashboardMessage,
                onRetry = {
                    scope.launch {
                        dashboardLoading = true
                        dashboardMessage = ""
                        val result = withContext(Dispatchers.IO) { fetchDashboard() }
                        dashboardLoading = false
                        if (result.success) {
                            loggedInName = result.name.ifBlank { loggedInName }
                            loggedInUsername = result.username.ifBlank { loggedInUsername }
                            loggedInRole = result.role
                            modules = result.modules
                            dashboardLoaded = true
                        } else {
                            dashboardMessage = result.message
                        }
                    }
                },
                onModuleClick = { module ->
                    selectedModule = module
                    selectedAdmission = null
                    when {
                        module.key.equals("events", ignoreCase = true) -> {
                            events = emptyList(); eventsMessage = ""; eventsPage = 1; eventsTotalPages = 1
                            loadEvents(1)
                        }
                        module.key.equals("magazines", ignoreCase = true) -> {
                            magazines = emptyList(); magazinesMessage = ""; magazinesPage = 1; magazinesTotalPages = 1
                            loadMagazines(1)
                        }
                        module.key.equals("seminars", ignoreCase = true) -> {
                            seminars = emptyList(); seminarsMessage = ""; seminarsPage = 1; seminarsTotalPages = 1
                            loadSeminars(1)
                        }
                        module.key.equals("gallery", ignoreCase = true) -> {
                            gallery = emptyList(); galleryMessage = ""; galleryPage = 1; galleryTotalPages = 1
                            loadGallery(1)
                        }
                        module.key.equals("contact_messages", ignoreCase = true) ||
                            module.key.equals("contact_inquiries", ignoreCase = true) -> {
                            contactMessages = emptyList(); contactMessagesMessage = ""
                            contactMessagesPage = 1; contactMessagesTotalPages = 1
                            loadContactMessages(1)
                        }
                        module.key.equals("admission_applications", ignoreCase = true) -> {
                            admissions = emptyList(); admissionsMessage = ""
                            admissionsPage = 1; admissionsTotalPages = 1
                            admissionSearch = ""; admissionTypeFilter = "all"; admissionStatusFilter = "all"
                            loadAdmissions(1, "", "all", "all")
                        }
                        module.key.equals("career_applications", ignoreCase = true) ||
                            module.key.equals("careers", ignoreCase = true) -> {
                            careerApplications = emptyList(); careersMessage = ""
                            loadCareers()
                        }
                    }
                }
            )
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (loginSucceeded && selectedModule != null) Color(0xFF080D18)
                    else PageBackground
                )
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NAVYUG",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyugNavy
            )

            Text(
                text = "GROUP OF EDUCATION",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = NavyugGold
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {

                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // =====================================
                    // LOGIN SCREEN
                    // =====================================

                    if (!loginSucceeded) {

                        Text(
                            text = "Admin Login",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyugNavy
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign in to your Navyug account",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                message = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Username") },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                message = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            singleLine = true,
                            enabled = !isLoading,
                            visualTransformation =
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {

                                if (
                                    username.trim().isEmpty() ||
                                    password.isEmpty()
                                ) {
                                    message =
                                        "Enter your username and password."
                                    return@Button
                                }

                                scope.launch {

                                    isLoading = true
                                    message = ""

                                    val result = withContext(
                                        Dispatchers.IO
                                    ) {
                                        performLogin(
                                            username.trim(),
                                            password
                                        )
                                    }

                                    isLoading = false

                                    if (result.success) {

                                        loginSucceeded = true
                                        loggedInName = result.name
                                        loggedInUsername = username.trim()
                                        password = ""
                                        message = ""

                                        dashboardLoading = true
                                        dashboardLoaded = false
                                        dashboardMessage = ""

                                        val dashboard = withContext(
                                            Dispatchers.IO
                                        ) {
                                            fetchDashboard()
                                        }

                                        dashboardLoading = false

                                        if (dashboard.success) {

                                            loggedInName =
                                                dashboard.name.ifBlank {
                                                    result.name
                                                }

                                            loggedInUsername =
                                                dashboard.username.ifBlank {
                                                    username.trim()
                                                }

                                            loggedInRole = dashboard.role
                                            modules = dashboard.modules
                                            dashboardLoaded = true

                                        } else {
                                            dashboardMessage =
                                                dashboard.message
                                        }

                                    } else {
                                        message = result.message
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyugNavy,
                                contentColor = Color.White
                            )
                        ) {

                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = NavyugGold,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        if (message.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = message,
                                color = Color(0xFFB42318),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Secure connection via HTTPS",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    // =====================================
                    // LOGGED-IN AREA
                    // =====================================

                    else {

                        if (selectedModule == null) {

                            Text(
                                text = "Welcome!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyugNavy
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = loggedInName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = NavyugNavy
                            )

                            if (loggedInUsername.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "@$loggedInUsername",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }

                            if (loggedInRole.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Role: $loggedInRole",
                                    color = NavyugNavy,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        NavyugNavy,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "ADMIN DASHBOARD",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            when {

                                dashboardLoading -> {
                                    CircularProgressIndicator(
                                        color = NavyugNavy
                                    )

                                    Spacer(
                                        modifier = Modifier.height(12.dp)
                                    )

                                    Text(
                                        text = "Loading your dashboard...",
                                        color = Color.Gray
                                    )
                                }

                                !dashboardLoaded -> {
                                    Text(
                                        text = dashboardMessage.ifBlank {
                                            "Unable to load the dashboard."
                                        },
                                        color = Color(0xFFB42318),
                                        fontSize = 14.sp
                                    )

                                    Spacer(
                                        modifier = Modifier.height(16.dp)
                                    )

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                dashboardLoading = true
                                                dashboardMessage = ""

                                                val result = withContext(
                                                    Dispatchers.IO
                                                ) {
                                                    fetchDashboard()
                                                }

                                                dashboardLoading = false

                                                if (result.success) {
                                                    loggedInName =
                                                        result.name.ifBlank {
                                                            loggedInName
                                                        }

                                                    loggedInUsername =
                                                        result.username.ifBlank {
                                                            loggedInUsername
                                                        }

                                                    loggedInRole = result.role
                                                    modules = result.modules
                                                    dashboardLoaded = true
                                                } else {
                                                    dashboardMessage =
                                                        result.message
                                                }
                                            }
                                        },
                                        enabled = !dashboardLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NavyugNavy
                                        )
                                    ) {
                                        Text("Retry Dashboard")
                                    }
                                }

                                else -> {
                                    Text(
                                        text = "Your Accessible Modules",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyugNavy
                                    )

                                    Spacer(
                                        modifier = Modifier.height(12.dp)
                                    )

                                    if (modules.isEmpty()) {
                                        Text(
                                            text = "No modules are assigned to this account.",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    } else {

                                        modules.forEach { module ->

                                            ModuleCard(
                                                module = module,
                                                onClick = {

                                                    selectedModule = module
                                                    selectedAdmission = null

                                                    when {
                                                        module.key.equals(
                                                            "events",
                                                            ignoreCase = true
                                                        ) -> {
                                                            events = emptyList()
                                                            eventsMessage = ""
                                                            eventsPage = 1
                                                            eventsTotalPages = 1
                                                            loadEvents(1)
                                                        }

                                                        module.key.equals(
                                                            "magazines",
                                                            ignoreCase = true
                                                        ) -> {
                                                            magazines = emptyList()
                                                            magazinesMessage = ""
                                                            magazinesPage = 1
                                                            magazinesTotalPages = 1
                                                            loadMagazines(1)
                                                        }

                                                        module.key.equals(
                                                            "seminars",
                                                            ignoreCase = true
                                                        ) -> {
                                                            seminars = emptyList()
                                                            seminarsMessage = ""
                                                            seminarsPage = 1
                                                            seminarsTotalPages = 1
                                                            loadSeminars(1)
                                                        }

                                                        module.key.equals(
                                                            "gallery",
                                                            ignoreCase = true
                                                        ) -> {
                                                            gallery = emptyList()
                                                            galleryMessage = ""
                                                            galleryPage = 1
                                                            galleryTotalPages = 1
                                                            loadGallery(1)
                                                        }

                                                        module.key.equals(
                                                            "contact_messages",
                                                            ignoreCase = true
                                                        ) ||
                                                                module.key.equals(
                                                                    "contact_inquiries",
                                                                    ignoreCase = true
                                                                ) -> {
                                                            contactMessages =
                                                                emptyList()
                                                            contactMessagesMessage =
                                                                ""
                                                            contactMessagesPage = 1
                                                            contactMessagesTotalPages = 1
                                                            loadContactMessages(1)
                                                        }

                                                        module.key.equals(
                                                            "admission_applications",
                                                            ignoreCase = true
                                                        ) -> {
                                                            admissions =
                                                                emptyList()
                                                            admissionsMessage =
                                                                ""
                                                            admissionsPage = 1
                                                            admissionsTotalPages = 1
                                                            admissionSearch = ""
                                                            admissionTypeFilter = "all"
                                                            admissionStatusFilter = "all"
                                                            loadAdmissions(1, "", "all", "all")
                                                        }

                                                        module.key.equals(
                                                            "career_applications",
                                                            ignoreCase = true
                                                        ) ||
                                                                module.key.equals(
                                                                    "careers",
                                                                    ignoreCase = true
                                                                ) -> {
                                                            careerApplications = emptyList()
                                                            careersMessage = ""
                                                            loadCareers()
                                                        }

                                                        else -> {
                                                            eventsMessage =
                                                                "${module.label} is not connected yet."
                                                        }
                                                    }
                                                }
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.height(10.dp)
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(20.dp)
                                    )

                                    Text(
                                        text = "Module access is based on your website account permissions.",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                        } else {

                            // SELECTED MODULE HEADER

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Button(
                                    onClick = {
                                        if (selectedAdmission != null) {
                                            selectedAdmission = null
                                        } else {
                                            selectedModule = null

                                            events = emptyList()
                                            eventsMessage = ""

                                            magazines = emptyList()
                                            magazinesMessage = ""

                                            seminars = emptyList()
                                            seminarsMessage = ""

                                            gallery = emptyList()
                                            galleryMessage = ""

                                            contactMessages = emptyList()
                                            contactMessagesMessage = ""

                                            admissions = emptyList()
                                            admissionsMessage = ""

                                            careerApplications = emptyList()
                                            careersMessage = ""
                                            careersLoading = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFDC134)
                                    )
                                ) {
                                    Text(
                                        if (selectedAdmission != null) {
                                            "Back"
                                        } else {
                                            "Dashboard"
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = selectedAdmission?.studentName
                                        ?: selectedModule!!.label,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF4F6FA)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            when {

                                selectedModule!!.key.equals(
                                    "events",
                                    ignoreCase = true
                                ) -> {

                                    Text(
                                        text = "Events Management",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF4F6FA)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "View events from your Navyug website.",
                                        color = Color(0xFF9AA9BC),
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (eventsLoading) {
                                        LoadingMessage("Loading events...")
                                    } else if (eventsMessage.isNotBlank()) {
                                        ErrorMessage(eventsMessage)
                                        RetryButton {
                                            loadEvents(eventsPage)
                                        }
                                    } else if (events.isEmpty()) {
                                        EmptyMessage("No events found.")
                                        RetryButton {
                                            loadEvents(1)
                                        }
                                    } else {
                                        Text(
                                            text = "${events.size} events on this page",
                                            color = Color(0xFF9AA9BC),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        events.forEach { event ->
                                            EventCard(event)
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        PaginationControls(
                                            page = eventsPage,
                                            totalPages = eventsTotalPages,
                                            onPrevious = {
                                                loadEvents(eventsPage - 1)
                                            },
                                            onNext = {
                                                loadEvents(eventsPage + 1)
                                            },
                                            enabled = !eventsLoading
                                        )

                                        RefreshButton("Refresh Events") {
                                            loadEvents(eventsPage)
                                        }
                                    }
                                }

                                selectedModule!!.key.equals(
                                    "magazines",
                                    ignoreCase = true
                                ) -> {

                                    Text(
                                        text = "Navyug Magazines",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF4F6FA)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "View and open magazine PDFs from your Navyug website.",
                                        color = Color(0xFF9AA9BC),
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (magazinesLoading) {
                                        LoadingMessage("Loading magazines...")
                                    } else if (magazinesMessage.isNotBlank()) {
                                        ErrorMessage(magazinesMessage)
                                        RetryButton {
                                            loadMagazines(magazinesPage)
                                        }
                                    } else if (magazines.isEmpty()) {
                                        EmptyMessage("No magazines found.")
                                        RetryButton {
                                            loadMagazines(1)
                                        }
                                    } else {
                                        Text(
                                            text = "${magazines.size} magazines on this page",
                                            color = Color(0xFF9AA9BC),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        magazines.forEach { magazine ->
                                            MagazineCard(magazine)
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        PaginationControls(
                                            page = magazinesPage,
                                            totalPages = magazinesTotalPages,
                                            onPrevious = {
                                                loadMagazines(magazinesPage - 1)
                                            },
                                            onNext = {
                                                loadMagazines(magazinesPage + 1)
                                            },
                                            enabled = !magazinesLoading
                                        )

                                        RefreshButton("Refresh Magazines") {
                                            loadMagazines(magazinesPage)
                                        }
                                    }
                                }

                                selectedModule!!.key.equals(
                                    "seminars",
                                    ignoreCase = true
                                ) -> {

                                    Text(
                                        text = "Seminars Management",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF4F6FA)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "View seminars and images from your Navyug website.",
                                        color = Color(0xFF9AA9BC),
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (seminarsLoading) {
                                        LoadingMessage("Loading seminars...")
                                    } else if (seminarsMessage.isNotBlank()) {
                                        ErrorMessage(seminarsMessage)
                                        RetryButton {
                                            loadSeminars(seminarsPage)
                                        }
                                    } else if (seminars.isEmpty()) {
                                        EmptyMessage("No seminars found.")
                                        RetryButton {
                                            loadSeminars(1)
                                        }
                                    } else {
                                        Text(
                                            text = "${seminars.size} seminars on this page",
                                            color = Color(0xFF9AA9BC),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        seminars.forEach { seminar ->
                                            SeminarCard(seminar)
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        PaginationControls(
                                            page = seminarsPage,
                                            totalPages = seminarsTotalPages,
                                            onPrevious = {
                                                loadSeminars(seminarsPage - 1)
                                            },
                                            onNext = {
                                                loadSeminars(seminarsPage + 1)
                                            },
                                            enabled = !seminarsLoading
                                        )

                                        RefreshButton("Refresh Seminars") {
                                            loadSeminars(seminarsPage)
                                        }
                                    }
                                }

                                selectedModule!!.key.equals(
                                    "gallery",
                                    ignoreCase = true
                                ) -> {

                                    Text(
                                        text = "Navyug Gallery",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF4F6FA)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "View photos from your Navyug website gallery.",
                                        color = Color(0xFF9AA9BC),
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (galleryLoading) {
                                        LoadingMessage("Loading gallery...")
                                    } else if (galleryMessage.isNotBlank()) {
                                        ErrorMessage(galleryMessage)
                                        RetryButton {
                                            loadGallery(galleryPage)
                                        }
                                    } else if (gallery.isEmpty()) {
                                        EmptyMessage("No gallery images found.")
                                        RetryButton {
                                            loadGallery(1)
                                        }
                                    } else {
                                        Text(
                                            text = "${gallery.size} gallery items on this page",
                                            color = Color(0xFF9AA9BC),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        gallery.forEach { item ->
                                            GalleryCard(item)
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        PaginationControls(
                                            page = galleryPage,
                                            totalPages = galleryTotalPages,
                                            onPrevious = {
                                                loadGallery(galleryPage - 1)
                                            },
                                            onNext = {
                                                loadGallery(galleryPage + 1)
                                            },
                                            enabled = !galleryLoading
                                        )

                                        RefreshButton("Refresh Gallery") {
                                            loadGallery(galleryPage)
                                        }
                                    }
                                }

                                selectedModule!!.key.equals(
                                    "contact_messages",
                                    ignoreCase = true
                                ) ||
                                        selectedModule!!.key.equals(
                                            "contact_inquiries",
                                            ignoreCase = true
                                        ) -> {

                                    Text(
                                        text = "Contact Messages",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF4F6FA)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "View enquiries submitted through your Navyug website contact form.",
                                        color = Color(0xFF9AA9BC),
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (contactMessagesLoading) {
                                        LoadingMessage("Loading contact messages...")
                                    } else if (contactMessagesMessage.isNotBlank()) {
                                        ErrorMessage(contactMessagesMessage)
                                        RetryButton {
                                            loadContactMessages(contactMessagesPage)
                                        }
                                    } else if (contactMessages.isEmpty()) {
                                        EmptyMessage("No contact messages found.")
                                        RetryButton {
                                            loadContactMessages(1)
                                        }
                                    } else {
                                        Text(
                                            text = "${contactMessages.size} messages on this page",
                                            color = Color(0xFF9AA9BC),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        contactMessages.forEach { item ->
                                            ContactMessageCard(item)
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        PaginationControls(
                                            page = contactMessagesPage,
                                            totalPages = contactMessagesTotalPages,
                                            onPrevious = {
                                                loadContactMessages(contactMessagesPage - 1)
                                            },
                                            onNext = {
                                                loadContactMessages(contactMessagesPage + 1)
                                            },
                                            enabled = !contactMessagesLoading
                                        )

                                        RefreshButton("Refresh Contact Messages") {
                                            loadContactMessages(contactMessagesPage)
                                        }
                                    }
                                }

                                selectedModule!!.key.equals(
                                    "admission_applications",
                                    ignoreCase = true
                                ) -> {

                                    if (selectedAdmission != null) {

                                        AdmissionDetailScreen(
                                            item = selectedAdmission!!
                                        )

                                    } else {

                                        Text(
                                            text = "Admission Applications",
                                            fontSize = 21.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF4F6FA)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "View school and college admission applications.",
                                            color = Color(0xFF9AA9BC),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(18.dp))

                                        OutlinedTextField(
                                            value = admissionSearch,
                                            onValueChange = {
                                                admissionSearch = it
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = {
                                                Text("Search applications")
                                            },
                                            placeholder = {
                                                Text("Name, application no., phone...")
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                loadAdmissions(
                                                    1,
                                                    admissionSearch,
                                                    admissionTypeFilter,
                                                    admissionStatusFilter
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFDC134)
                                            )
                                        ) {
                                            Text("Search")
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "Admission Type",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF4F6FA)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        FilterChoiceRow(
                                            options = listOf(
                                                "all" to "All",
                                                "School" to "School",
                                                "College" to "College"
                                            ),
                                            selected = admissionTypeFilter,
                                            onSelected = {
                                                admissionTypeFilter = it
                                                loadAdmissions(
                                                    1,
                                                    admissionSearch,
                                                    it,
                                                    admissionStatusFilter
                                                )
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "Application Status",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF4F6FA)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            FilterChoiceRow(
                                                options = listOf(
                                                    "all" to "All",
                                                    "Pending" to "Pending",
                                                    "Under Review" to "Under Review"
                                                ),
                                                selected = admissionStatusFilter,
                                                onSelected = {
                                                    admissionStatusFilter = it
                                                    loadAdmissions(
                                                        1,
                                                        admissionSearch,
                                                        admissionTypeFilter,
                                                        it
                                                    )
                                                }
                                            )

                                            FilterChoiceRow(
                                                options = listOf(
                                                    "Approved" to "Approved",
                                                    "Rejected" to "Rejected"
                                                ),
                                                selected = admissionStatusFilter,
                                                onSelected = {
                                                    admissionStatusFilter = it
                                                    loadAdmissions(
                                                        1,
                                                        admissionSearch,
                                                        admissionTypeFilter,
                                                        it
                                                    )
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))

                                        if (admissionsLoading) {

                                            LoadingMessage(
                                                "Loading admission applications..."
                                            )

                                        } else if (admissionsMessage.isNotBlank()) {

                                            ErrorMessage(admissionsMessage)

                                            RetryButton {
                                                loadAdmissions(
                                                    admissionsPage,
                                                    admissionSearch,
                                                    admissionTypeFilter,
                                                    admissionStatusFilter
                                                )
                                            }

                                        } else if (admissions.isEmpty()) {

                                            EmptyMessage(
                                                "No admission applications found."
                                            )

                                            RetryButton {
                                                loadAdmissions(
                                                    1,
                                                    admissionSearch,
                                                    admissionTypeFilter,
                                                    admissionStatusFilter
                                                )
                                            }

                                        } else {

                                            Text(
                                                text = "${admissions.size} applications on this page",
                                                color = Color(0xFF9AA9BC),
                                                fontSize = 13.sp
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            admissions.forEach { item ->

                                                AdmissionCard(
                                                    item = item,
                                                    onClick = {
                                                        selectedAdmission = item
                                                    }
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(14.dp)
                                                )
                                            }

                                            PaginationControls(
                                                page = admissionsPage,
                                                totalPages = admissionsTotalPages,
                                                onPrevious = {
                                                    loadAdmissions(
                                                        admissionsPage - 1
                                                    )
                                                },
                                                onNext = {
                                                    loadAdmissions(
                                                        admissionsPage + 1
                                                    )
                                                },
                                                enabled = !admissionsLoading
                                            )

                                            RefreshButton(
                                                "Refresh Admissions"
                                            ) {
                                                loadAdmissions(
                                                    admissionsPage,
                                                    admissionSearch,
                                                    admissionTypeFilter,
                                                    admissionStatusFilter
                                                )
                                            }
                                        }
                                    }
                                }


                                selectedModule!!.key.equals(
                                    "career_applications",
                                    ignoreCase = true
                                ) || selectedModule!!.key.equals(
                                    "careers",
                                    ignoreCase = true
                                ) -> {

                                    CareersScreen(
                                        applications = careerApplications,
                                        isLoading = careersLoading,
                                        message = careersMessage,
                                        onRefresh = {
                                            loadCareers()
                                        },
                                        onOpenResume = { path ->

                                            val resumeUrl = getFileUrl(path)

                                            try {
                                                if (resumeUrl.isNotBlank()) {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(resumeUrl)
                                                    )
                                                    careerContext.startActivity(intent)
                                                }
                                            } catch (_: Exception) {
                                                android.widget.Toast.makeText(
                                                    careerContext,
                                                    "Unable to open resume.",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                }

                                else -> {
                                    Text(
                                        text = selectedModule!!.label,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF4F6FA)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "This module will be connected in a later step.",
                                        color = Color(0xFF9AA9BC),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // SIGN OUT

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {

                                scope.launch {

                                    withContext(Dispatchers.IO) {
                                        try {
                                            getSessionCookieManager()
                                                .cookieStore
                                                .removeAll()
                                        } catch (_: Exception) {
                                            // Continue signing out locally.
                                        }
                                    }

                                    loginSucceeded = false
                                    loggedInName = ""
                                    loggedInUsername = ""
                                    loggedInRole = ""

                                    username = ""
                                    password = ""
                                    message = ""

                                    dashboardMessage = ""
                                    dashboardLoaded = false
                                    dashboardLoading = false
                                    modules = emptyList()

                                    selectedModule = null
                                    selectedAdmission = null

                                    events = emptyList()
                                    eventsMessage = ""
                                    eventsLoading = false
                                    eventsPage = 1
                                    eventsTotalPages = 1

                                    magazines = emptyList()
                                    magazinesMessage = ""
                                    magazinesLoading = false
                                    magazinesPage = 1
                                    magazinesTotalPages = 1

                                    seminars = emptyList()
                                    seminarsMessage = ""
                                    seminarsLoading = false
                                    seminarsPage = 1
                                    seminarsTotalPages = 1

                                    gallery = emptyList()
                                    galleryMessage = ""
                                    galleryLoading = false
                                    galleryPage = 1
                                    galleryTotalPages = 1

                                    contactMessages = emptyList()
                                    contactMessagesMessage = ""
                                    contactMessagesLoading = false
                                    contactMessagesPage = 1
                                    contactMessagesTotalPages = 1

                                    admissions = emptyList()
                                    admissionsMessage = ""
                                    admissionsLoading = false

                                    careerApplications = emptyList()
                                    careersMessage = ""
                                    careersLoading = false
                                    admissionsPage = 1
                                    admissionsTotalPages = 1
                                    admissionSearch = ""
                                    admissionTypeFilter = "all"
                                    admissionStatusFilter = "all"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB42318)
                            )
                        ) {
                            Text("Sign Out")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Navyug Group of Education",
                color = Color(0xFFF4F6FA),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}

// ---------------------------------------------------------
// DARK PREMIUM DASHBOARD
// ---------------------------------------------------------

@Composable
private fun DarkPremiumDashboard(
    name: String,
    username: String,
    role: String,
    modules: List<DashboardModule>,
    loading: Boolean,
    loaded: Boolean,
    message: String,
    onRetry: () -> Unit,
    onModuleClick: (DashboardModule) -> Unit
) {
    val background = Color(0xFF080D18)
    val panel = Color(0xFF111B2B)
    val panelAlt = Color(0xFF172438)
    val gold = Color(0xFFFDC134)
    val muted = Color(0xFF9AA9BC)
    val white = Color(0xFFF5F7FB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(gold, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = background, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("NAVYUG", color = white, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("GROUP OF EDUCATION", color = gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
            }
            Surface(color = Color(0xFF183329), shape = RoundedCornerShape(30.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(7.dp).background(Color(0xFF55D69E), RoundedCornerShape(50)))
                    Spacer(Modifier.width(7.dp))
                    Text("SECURE", color = Color(0xFF8DE2B8), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Text("ADMINISTRATION", color = gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(7.dp))
        Text(
            text = "Welcome back,\n${name.ifBlank { "Administrator" }}",
            color = white,
            fontSize = 29.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.Bold
        )
        if (username.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("@$username", color = muted, fontSize = 13.sp)
        }

        Spacer(Modifier.height(22.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = panel,
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(Color(0xFF1B304B), Color(0xFF111B2B), Color(0xFF25213A))))
                    .padding(20.dp)
            ) {
                Column {
                    Text("CONTROL CENTER", color = gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(9.dp))
                    Text("Your institution.\nOne place to manage it.", color = white, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(17.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0x33FDC134), shape = RoundedCornerShape(9.dp)) {
                            Text(
                                text = role.ifBlank { "ADMIN ACCOUNT" }.uppercase(),
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                color = gold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text("NAVYUG ADMIN", color = muted, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                value = modules.size.toString(),
                label = "ACCESSIBLE MODULES",
                accent = gold,
                panel = panelAlt,
                textColor = white,
                muted = muted
            )
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                value = if (loaded) "READY" else if (loading) "SYNC" else "—",
                label = "DASHBOARD STATUS",
                accent = Color(0xFF70D6A8),
                panel = panelAlt,
                textColor = white,
                muted = muted
            )
        }

        Spacer(Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("MANAGEMENT", color = gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(5.dp))
                Text("Your modules", color = white, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text("${modules.size} AVAILABLE", color = muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(14.dp))

        when {
            loading -> {
                Surface(color = panel, shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = gold, strokeWidth = 2.dp)
                        Spacer(Modifier.width(13.dp))
                        Text("Syncing your dashboard modules...", color = muted, fontSize = 13.sp)
                    }
                }
            }
            !loaded -> {
                Surface(color = panel, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(message.ifBlank { "Unable to load your dashboard." }, color = Color(0xFFFF9B9B), fontSize = 13.sp)
                        Spacer(Modifier.height(13.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = background),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Retry connection", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            modules.isEmpty() -> {
                Surface(color = panel, shape = RoundedCornerShape(18.dp)) {
                    Text(
                        "No modules are assigned to this account.",
                        modifier = Modifier.padding(20.dp),
                        color = muted,
                        fontSize = 13.sp
                    )
                }
            }
            else -> {
                modules.chunked(2).forEach { rowModules ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowModules.forEach { module ->
                            DarkModuleTile(
                                modifier = Modifier.weight(1f),
                                module = module,
                                onClick = { onModuleClick(module) }
                            )
                        }
                        if (rowModules.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Surface(color = Color(0xFF101827), shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("N", color = gold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Navyug Group of Education", color = white, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Administration app", color = muted, fontSize = 10.sp)
                }
                Text("v1.0", color = muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DashboardMetricCard(
    modifier: Modifier,
    value: String,
    label: String,
    accent: Color,
    panel: Color,
    textColor: Color,
    muted: Color
) {
    Surface(modifier = modifier, color = panel, shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(value, color = accent, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(5.dp))
            Text(label, color = muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
        }
    }
}

@Composable
private fun DarkModuleTile(
    modifier: Modifier,
    module: DashboardModule,
    onClick: () -> Unit
) {
    val white = Color(0xFFF4F6FA)
    val muted = Color(0xFF9AA9BC)
    val gold = Color(0xFFFDC134)
    val icon = when (module.key.lowercase()) {
        "events" -> "◷"
        "magazines" -> "▤"
        "seminars" -> "◉"
        "gallery" -> "▧"
        "contact_messages", "contact_inquiries" -> "✉"
        "admission_applications" -> "＋"
        "career_applications", "careers" -> "▣"
        else -> "◆"
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color(0xFF121E30),
        shape = RoundedCornerShape(19.dp),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(15.dp).height(166.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF26354B), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, color = gold, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(15.dp))
            Text(module.label, color = white, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, lineHeight = 18.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                module.subtitle.ifBlank { "Open module" },
                color = muted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                minLines = 2
            )
            Spacer(Modifier.weight(1f))
            Text("OPEN  →", color = gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// ---------------------------------------------------------
// FILTER CHOICE ROW
// ---------------------------------------------------------

@Composable
private fun FilterChoiceRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->

            val isSelected = selected == option.first

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSelected(option.first)
                    },
                shape = RoundedCornerShape(9.dp),
                color = if (isSelected) {
                    NavyugNavy
                } else {
                    Color(0xFFF0F2F5)
                }
            ) {
                Text(
                    text = option.second,
                    modifier = Modifier.padding(
                        horizontal = 5.dp,
                        vertical = 10.dp
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        Color.White
                    } else {
                        NavyugNavy
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------
// COMMON LOADING / ERROR / PAGINATION COMPONENTS
// ---------------------------------------------------------

@Composable
private fun LoadingMessage(message: String) {
    CircularProgressIndicator(color = NavyugNavy)

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = message,
        color = Color.Gray
    )
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = Color(0xFFB42318),
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun EmptyMessage(message: String) {
    Text(
        text = message,
        color = Color.Gray,
        fontSize = 15.sp
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun RetryButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = NavyugNavy
        )
    ) {
        Text("Retry")
    }
}

@Composable
private fun RefreshButton(
    label: String,
    onClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = NavyugGold,
            contentColor = NavyugNavy
        )
    ) {
        Text(label)
    }
}

@Composable
private fun PaginationControls(
    page: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    enabled: Boolean
) {
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            onClick = onPrevious,
            enabled = page > 1 && enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyugNavy
            )
        ) {
            Text("Previous")
        }

        Text(
            text = "$page / $totalPages",
            fontWeight = FontWeight.Bold,
            color = NavyugNavy
        )

        Button(
            onClick = onNext,
            enabled = page < totalPages && enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyugNavy
            )
        ) {
            Text("Next")
        }
    }
}

// ---------------------------------------------------------
// DASHBOARD MODULE CARD
// ---------------------------------------------------------

@Composable
private fun ModuleCard(
    module: DashboardModule,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        shadowElevation = 1.dp
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        NavyugGold.copy(alpha = 0.22f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = module.label
                        .firstOrNull()
                        ?.uppercaseChar()
                        ?.toString() ?: "N",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyugNavy
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = module.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyugNavy
                )

                if (module.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = module.subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "›",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NavyugGold
            )
        }
    }
}

// ---------------------------------------------------------
// EVENT CARD WITH IMAGE PREVIEWS
// ---------------------------------------------------------

@Composable
private fun EventCard(event: EventRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = event.title.ifBlank { "Untitled Event" },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (event.eventDate.isNotBlank()) {
                Text(
                    text = "Date: ${event.eventDate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFDC134)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = Color(0xFFD0D8E4)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (event.files.isNotEmpty()) {

                Text(
                    text = "Event Images (${event.files.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF4F6FA)
                )

                Spacer(modifier = Modifier.height(10.dp))

                event.files.forEachIndexed { index, file ->

                    val imageUrl = getEventImageUrl(file)

                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription =
                                "${event.title} image ${index + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else {
                Text(
                    text = "No attached images or files.",
                    fontSize = 12.sp,
                    color = Color(0xFF9AA9BC)
                )
            }

            if (event.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Created: ${event.createdAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// MAGAZINE CARD WITH PDF OPENING
// ---------------------------------------------------------

@Composable
private fun MagazineCard(magazine: MagazineRecord) {

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = magazine.title.ifBlank {
                    "Untitled Magazine"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (magazine.issueDate.isNotBlank()) {
                Text(
                    text = "Issue Date: ${magazine.issueDate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFDC134)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (magazine.description.isNotBlank()) {
                Text(
                    text = magazine.description,
                    fontSize = 14.sp,
                    color = Color(0xFFD0D8E4)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (magazine.files.isNotEmpty()) {

                Text(
                    text = "Available PDF Files (${magazine.files.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF4F6FA)
                )

                Spacer(modifier = Modifier.height(10.dp))

                magazine.files.forEachIndexed { index, file ->

                    val pdfUrl = getMagazinePdfUrl(file)

                    if (pdfUrl.isNotBlank()) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF202E43),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "PDF ${index + 1}",
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF4F6FA)
                            )

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(pdfUrl)
                                        )

                                        context.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                "Open Magazine PDF"
                                            )
                                        )
                                    } catch (_: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "No app available to open this PDF.",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyugNavy
                                )
                            ) {
                                Text("Open PDF")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Text(
                    text = "No PDF attached to this magazine.",
                    fontSize = 12.sp,
                    color = Color(0xFF9AA9BC)
                )
            }

            if (magazine.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Created: ${magazine.createdAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// SEMINAR CARD WITH IMAGE
// ---------------------------------------------------------

@Composable
private fun SeminarCard(seminar: SeminarRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = seminar.title.ifBlank {
                    "Untitled Seminar"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (seminar.seminarDate.isNotBlank()) {
                Text(
                    text = "Seminar Date: ${seminar.seminarDate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFDC134)
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            val imageUrl = getSeminarImageUrl(seminar.imagePath)

            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${seminar.title} image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (seminar.description.isNotBlank()) {
                Text(
                    text = seminar.description,
                    fontSize = 14.sp,
                    color = Color(0xFFD0D8E4)
                )
            } else {
                Text(
                    text = "No description available.",
                    fontSize = 12.sp,
                    color = Color(0xFF9AA9BC)
                )
            }

            if (seminar.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Created: ${seminar.createdAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// GALLERY CARD WITH IMAGE
// ---------------------------------------------------------

@Composable
private fun GalleryCard(item: GalleryRecord) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = item.title.ifBlank {
                    "Untitled Gallery Image"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val imageUrl = getGalleryImageUrl(item.imagePath)

            if (imageUrl.isNotBlank()) {

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${item.title} gallery image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            Color(0xFFE8EDF2),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Image not available",
                        color = Color(0xFF9AA9BC),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = Color(0xFFD0D8E4)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (item.createdAt.isNotBlank()) {
                Text(
                    text = "Added: ${item.createdAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// CONTACT MESSAGE CARD
// ---------------------------------------------------------

@Composable
private fun ContactMessageCard(
    item: ContactMessageRecord
) {

    val context = LocalContext.current

    val isRead = item.status.equals(
        "read",
        ignoreCase = true
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = item.name.ifBlank {
                        "Unknown Sender"
                    },
                    modifier = Modifier.weight(1f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF4F6FA)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isRead) {
                        Color(0xFFE5E7EB)
                    } else {
                        Color(0xFFFFE8B3)
                    }
                ) {
                    Text(
                        text = item.status.uppercase(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRead) {
                            Color(0xFF475467)
                        } else {
                            Color(0xFF8A6500)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Subject",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9AA9BC)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = item.subject.ifBlank {
                    "No subject"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Message",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9AA9BC)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message.ifBlank {
                    "No message"
                },
                fontSize = 14.sp,
                color = Color(0xFFD0D8E4)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Contact Details",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Phone: ${item.phone}",
                fontSize = 13.sp,
                color = Color(0xFFD0D8E4)
            )

            Text(
                text = "Email: ${item.email}",
                fontSize = 13.sp,
                color = Color(0xFFD0D8E4)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${Uri.encode(item.phone)}")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                "Unable to open phone dialer.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = item.phone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyugNavy
                    )
                ) {
                    Text("Call")
                }

                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:${Uri.encode(item.email)}")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                "Unable to open email app.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = item.email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyugGold,
                        contentColor = NavyugNavy
                    )
                ) {
                    Text("Email")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.createdAt.isNotBlank()) {
                Text(
                    text = "Received: ${item.createdAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }

            if (item.readAt.isNotBlank()) {
                Text(
                    text = "Read at: ${item.readAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// ADMISSION APPLICATION CARD
// ---------------------------------------------------------

@Composable
private fun AdmissionCard(
    item: AdmissionRecord,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier
                .defaultMinSize(minHeight = 196.dp)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.studentName.ifBlank {
                            "Unnamed Student"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF4F6FA)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.applicationNo.ifBlank {
                            "Application #${item.id}"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF9AA9BC)
                    )
                }

                AdmissionStatusBadge(item.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${item.admissionType} • ${item.applyingFor}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Academic Year: ${item.academicYear}",
                fontSize = 13.sp,
                color = Color(0xFFD0D8E4)
            )

            Text(
                text = "Guardian Phone: ${item.guardianPhone}",
                fontSize = 13.sp,
                color = Color(0xFFD0D8E4)
            )

            if (item.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Applied: ${item.createdAt}",
                    fontSize = 11.sp,
                    color = Color(0xFF9AA9BC)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyugNavy
                )
            ) {
                Text("View Full Application")
            }
        }
    }
}

// ---------------------------------------------------------
// ADMISSION STATUS BADGE
// ---------------------------------------------------------

@Composable
private fun AdmissionStatusBadge(status: String) {

    val badgeColor = when (status) {
        "Approved" -> Color(0xFFDDF5E5)
        "Rejected" -> Color(0xFFFCE4E4)
        "Under Review" -> Color(0xFFE2ECFF)
        else -> Color(0xFFFFE8B3)
    }

    val textColor = when (status) {
        "Approved" -> Color(0xFF16703C)
        "Rejected" -> Color(0xFFB42318)
        "Under Review" -> Color(0xFF2457A6)
        else -> Color(0xFF8A6500)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = badgeColor
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 6.dp
            ),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ---------------------------------------------------------
// ADMISSION DETAIL SCREEN
// ---------------------------------------------------------

@Composable
private fun AdmissionDetailScreen(
    item: AdmissionRecord
) {

    val context = LocalContext.current

    Text(
        text = "Application Details",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFF4F6FA)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = item.applicationNo.ifBlank {
            "Application #${item.id}"
        },
        fontSize = 14.sp,
        color = Color(0xFF9AA9BC)
    )

    Spacer(modifier = Modifier.height(12.dp))

    AdmissionStatusBadge(item.status)

    Spacer(modifier = Modifier.height(18.dp))

    if (item.studentPhoto.isNotBlank()) {

        AsyncImage(
            model = getAdmissionFileUrl(item.studentPhoto),
            contentDescription = "Student photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(18.dp))
    }

    AdmissionDetailSection("Admission Information") {
        AdmissionDetailRow("Application Number", item.applicationNo)
        AdmissionDetailRow("Admission Type", item.admissionType)
        AdmissionDetailRow("Academic Year", item.academicYear)
        AdmissionDetailRow("Applying For", item.applyingFor)
        AdmissionDetailRow("Application Status", item.status)
    }

    AdmissionDetailSection("Student Information") {
        AdmissionDetailRow("Student Name", item.studentName)
        AdmissionDetailRow("Date of Birth", item.dob)
        AdmissionDetailRow("Gender", item.gender)
    }

    AdmissionDetailSection("Parent / Guardian Information") {
        AdmissionDetailRow("Father Name", item.fatherName)
        AdmissionDetailRow("Mother Name", item.motherName)
        AdmissionDetailRow("Guardian Phone", item.guardianPhone)
        AdmissionDetailRow("Guardian Email", item.guardianEmail)
    }

    AdmissionDetailSection("Address") {
        AdmissionDetailRow("Residential Address", item.address)
    }

    AdmissionDetailSection("Previous Education") {
        AdmissionDetailRow(
            "Previous Institution",
            item.previousInstitution
        )
        AdmissionDetailRow("Previous Class", item.previousClass)
        AdmissionDetailRow("Previous Result", item.previousResult)
    }

    AdmissionDetailSection("Uploaded Documents") {

        AdmissionDocumentButton(
            label = "Student Photo",
            path = item.studentPhoto,
            isImage = true
        )

        AdmissionDocumentButton(
            label = "Birth Certificate",
            path = item.birthCertificate
        )

        AdmissionDocumentButton(
            label = "Marksheet",
            path = item.marksheet
        )

        AdmissionDocumentButton(
            label = "Leaving Certificate",
            path = item.leavingCertificate
        )
    }

    AdmissionDetailSection("Admin Remarks") {
        AdmissionDetailRow(
            "Remarks",
            item.adminRemarks.ifBlank {
                "No admin remarks."
            }
        )
    }

    AdmissionDetailSection("Application Dates") {
        AdmissionDetailRow("Submitted", item.createdAt)
        AdmissionDetailRow("Last Updated", item.updatedAt)
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = {
            try {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse(
                        "tel:${Uri.encode(item.guardianPhone)}"
                    )
                )
                context.startActivity(intent)
            } catch (_: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "Unable to open phone dialer.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = item.guardianPhone.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFDC134)
        )
    ) {
        Text("Call Guardian")
    }
}

// ---------------------------------------------------------
// ADMISSION DETAIL SECTION
// ---------------------------------------------------------

@Composable
private fun AdmissionDetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF121E30),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4F6FA)
            )

            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}

// ---------------------------------------------------------
// ADMISSION DETAIL ROW
// ---------------------------------------------------------

@Composable
private fun AdmissionDetailRow(
    label: String,
    value: String
) {
    if (value.isBlank() || value == "null") return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp)
    ) {

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9AA9BC)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFFD0D8E4)
        )
    }
}

// ---------------------------------------------------------
// ADMISSION DOCUMENT BUTTON
// ---------------------------------------------------------

@Composable
private fun AdmissionDocumentButton(
    label: String,
    path: String,
    isImage: Boolean = false
) {

    val context = LocalContext.current

    val available = path.isNotBlank() && path != "null"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF4F6FA)
            )

            Button(
                onClick = {
                    try {
                        val fileUrl = getAdmissionFileUrl(path)

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(fileUrl)
                        )

                        context.startActivity(
                            Intent.createChooser(
                                intent,
                                "Open $label"
                            )
                        )

                    } catch (_: Exception) {
                        android.widget.Toast.makeText(
                            context,
                            "Unable to open $label.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                enabled = available,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26354B)
                )
            ) {
                Text(
                    if (available) {
                        if (isImage) "Open Photo" else "Open"
                    } else {
                        "Not Uploaded"
                    },
                    fontSize = 11.sp
                )
            }
        }

        if (isImage && available) {
            AsyncImage(
                model = getAdmissionFileUrl(path),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ---------------------------------------------------------
// LOGIN API
// ---------------------------------------------------------

private fun performLogin(
    username: String,
    password: String
): LoginResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        connection =
            URL(LOGIN_URL).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true

        connection.setRequestProperty(
            "Content-Type",
            "application/json; charset=UTF-8"
        )

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val requestBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }.toString()

        connection.outputStream.use { output ->
            output.write(requestBody.toByteArray(Charsets.UTF_8))
        }

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status in 200..299 &&
            json.optBoolean("success", false)
        ) {

            val user = json.optJSONObject("user")

            LoginResult(
                success = true,
                message = "Login successful.",
                name = user?.optString("name")
                    ?.takeIf {
                        it.isNotBlank() && it != "null"
                    } ?: username
            )

        } else {

            LoginResult(
                success = false,
                message = json.optString(
                    "message",
                    "Login failed. Please try again."
                )
            )
        }

    } catch (_: Exception) {

        LoginResult(
            success = false,
            message = "Could not connect to the server. Check your internet and try again."
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// DASHBOARD API
// ---------------------------------------------------------

private fun fetchDashboard(): DashboardResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        connection =
            URL(DASHBOARD_URL).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            DashboardResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load the dashboard."
                )
            )

        } else {

            val user = json.optJSONObject("user")
            val jsonModules = json.optJSONArray("modules")
            val parsedModules = mutableListOf<DashboardModule>()

            if (jsonModules != null) {
                for (index in 0 until jsonModules.length()) {

                    val item = jsonModules.optJSONObject(index)
                        ?: continue

                    parsedModules.add(
                        DashboardModule(
                            key = item.optString("key"),
                            label = item.optString("label"),
                            subtitle = item.optString("subtitle"),
                            color = item.optString("color")
                        )
                    )
                }
            }

            DashboardResult(
                success = true,
                message = "Dashboard loaded successfully.",
                name = user?.optString("name") ?: "",
                username = user?.optString("username") ?: "",
                role = user?.optString("role") ?: "",
                modules = parsedModules
            )
        }

    } catch (_: Exception) {

        DashboardResult(
            success = false,
            message = "Could not load the dashboard. Check your connection and retry."
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// EVENTS API
// ---------------------------------------------------------

private fun fetchEvents(page: Int = 1): EventsResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        val url = "$EVENTS_URL?page=$page&per_page=10"

        connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            EventsResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load Events."
                )
            )

        } else {

            val eventsArray = json.optJSONArray("events")
            val parsedEvents = mutableListOf<EventRecord>()

            if (eventsArray != null) {
                for (index in 0 until eventsArray.length()) {

                    val item = eventsArray.optJSONObject(index)
                        ?: continue

                    val filesArray = item.optJSONArray("files")
                    val files = mutableListOf<String>()

                    if (filesArray != null) {
                        for (
                        fileIndex in 0 until filesArray.length()
                        ) {
                            val file = filesArray.optString(fileIndex)

                            if (
                                file.isNotBlank() &&
                                file != "null"
                            ) {
                                files.add(file)
                            }
                        }
                    }

                    parsedEvents.add(
                        EventRecord(
                            id = item.optInt("id"),
                            title = item.optString("title"),
                            eventDate = item.optString("event_date"),
                            description = item.optString("description"),
                            files = files,
                            createdAt = item.optString("created_at")
                        )
                    )
                }
            }

            val pagination = json.optJSONObject("pagination")

            EventsResult(
                success = true,
                message = "Events loaded successfully.",
                events = parsedEvents,
                currentPage = pagination?.optInt("page", page) ?: page,
                totalPages = pagination?.optInt("total_pages", 1) ?: 1
            )
        }

    } catch (e: Exception) {

        EventsResult(
            success = false,
            message = "Could not connect to the Events server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// MAGAZINES API
// ---------------------------------------------------------

private fun fetchMagazines(page: Int = 1): MagazinesResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        val url = "$MAGAZINES_URL?page=$page&per_page=10"

        connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            MagazinesResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load Magazines."
                )
            )

        } else {

            val magazinesArray = json.optJSONArray("magazines")
            val parsedMagazines = mutableListOf<MagazineRecord>()

            if (magazinesArray != null) {
                for (index in 0 until magazinesArray.length()) {

                    val item = magazinesArray.optJSONObject(index)
                        ?: continue

                    val filesArray = item.optJSONArray("files")
                    val files = mutableListOf<String>()

                    if (filesArray != null) {
                        for (
                        fileIndex in 0 until filesArray.length()
                        ) {
                            val file = filesArray.optString(fileIndex)

                            if (
                                file.isNotBlank() &&
                                file != "null"
                            ) {
                                files.add(file)
                            }
                        }
                    }

                    parsedMagazines.add(
                        MagazineRecord(
                            id = item.optInt("id"),
                            title = item.optString("title"),
                            issueDate = item.optString("issue_date"),
                            description = item.optString("description"),
                            files = files,
                            createdAt = item.optString("created_at")
                        )
                    )
                }
            }

            val pagination = json.optJSONObject("pagination")

            MagazinesResult(
                success = true,
                message = "Magazines loaded successfully.",
                magazines = parsedMagazines,
                currentPage = pagination?.optInt("page", page) ?: page,
                totalPages = pagination?.optInt("total_pages", 1) ?: 1
            )
        }

    } catch (e: Exception) {

        MagazinesResult(
            success = false,
            message = "Could not connect to the Magazines server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// SEMINARS API
// ---------------------------------------------------------

private fun fetchSeminars(page: Int = 1): SeminarsResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        val url = "$SEMINARS_URL?page=$page&per_page=10"

        connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            SeminarsResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load Seminars."
                )
            )

        } else {

            val seminarsArray = json.optJSONArray("seminars")
            val parsedSeminars = mutableListOf<SeminarRecord>()

            if (seminarsArray != null) {
                for (index in 0 until seminarsArray.length()) {

                    val item = seminarsArray.optJSONObject(index)
                        ?: continue

                    parsedSeminars.add(
                        SeminarRecord(
                            id = item.optInt("id"),
                            title = item.optString("title"),
                            seminarDate = item.optString("seminar_date"),
                            description = item.optString("description"),
                            imagePath = item.optString("image_path"),
                            createdAt = item.optString("created_at")
                        )
                    )
                }
            }

            val pagination = json.optJSONObject("pagination")

            SeminarsResult(
                success = true,
                message = "Seminars loaded successfully.",
                seminars = parsedSeminars,
                currentPage = pagination?.optInt("page", page) ?: page,
                totalPages = pagination?.optInt("total_pages", 1) ?: 1
            )
        }

    } catch (e: Exception) {

        SeminarsResult(
            success = false,
            message = "Could not connect to the Seminars server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// GALLERY API
// ---------------------------------------------------------

private fun fetchGallery(page: Int = 1): GalleryResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        val url = "$GALLERY_URL?page=$page&per_page=10"

        connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            GalleryResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load Gallery."
                )
            )

        } else {

            val galleryArray = json.optJSONArray("gallery")
            val parsedGallery = mutableListOf<GalleryRecord>()

            if (galleryArray != null) {
                for (index in 0 until galleryArray.length()) {

                    val item = galleryArray.optJSONObject(index)
                        ?: continue

                    parsedGallery.add(
                        GalleryRecord(
                            id = item.optInt("id"),
                            title = item.optString("title"),
                            description = item.optString("description"),
                            imagePath = item.optString("image_path"),
                            createdAt = item.optString("created_at")
                        )
                    )
                }
            }

            val pagination = json.optJSONObject("pagination")

            GalleryResult(
                success = true,
                message = "Gallery loaded successfully.",
                gallery = parsedGallery,
                currentPage = pagination?.optInt("page", page) ?: page,
                totalPages = pagination?.optInt("total_pages", 1) ?: 1
            )
        }

    } catch (e: Exception) {

        GalleryResult(
            success = false,
            message = "Could not connect to the Gallery server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// CONTACT MESSAGES API
// ---------------------------------------------------------

private fun fetchContactMessages(
    page: Int = 1
): ContactMessagesResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        val url =
            "$CONTACT_MESSAGES_URL?page=$page&per_page=10"

        connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            status !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            ContactMessagesResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load Contact Messages."
                )
            )

        } else {

            val messagesArray = json.optJSONArray("messages")
            val parsedMessages =
                mutableListOf<ContactMessageRecord>()

            if (messagesArray != null) {

                for (index in 0 until messagesArray.length()) {

                    val item =
                        messagesArray.optJSONObject(index)
                            ?: continue

                    parsedMessages.add(
                        ContactMessageRecord(
                            id = item.optInt("id"),
                            name = item.optString("name"),
                            phone = item.optString("phone"),
                            email = item.optString("email"),
                            subject = item.optString("subject"),
                            message = item.optString("message"),
                            status = item.optString("status"),
                            createdAt = item.optString("created_at"),
                            readAt = item.optString("read_at")
                        )
                    )
                }
            }

            val pagination =
                json.optJSONObject("pagination")

            ContactMessagesResult(
                success = true,
                message = "Contact Messages loaded successfully.",
                messages = parsedMessages,
                currentPage = pagination?.optInt(
                    "page",
                    page
                ) ?: page,
                totalPages = pagination?.optInt(
                    "total_pages",
                    1
                ) ?: 1
            )
        }

    } catch (e: Exception) {

        ContactMessagesResult(
            success = false,
            message = "Could not connect to the Contact Messages server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// ADMISSIONS API
// ---------------------------------------------------------

private fun fetchAdmissions(
    page: Int = 1,
    search: String = "",
    admissionType: String = "all",
    status: String = "all"
): AdmissionsResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        val encodedSearch = URLEncoder.encode(
            search,
            "UTF-8"
        )

        val encodedType = URLEncoder.encode(
            admissionType,
            "UTF-8"
        )

        val encodedStatus = URLEncoder.encode(
            status,
            "UTF-8"
        )

        val url =
            "$ADMISSIONS_URL?page=$page&per_page=10" +
                    "&search=$encodedSearch" +
                    "&admission_type=$encodedType" +
                    "&status=$encodedStatus"

        connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 20000
        connection.readTimeout = 20000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val responseCode = connection.responseCode

        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        val json = JSONObject(responseBody)

        if (
            responseCode !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            AdmissionsResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load admission applications."
                )
            )

        } else {

            val applicationsArray =
                json.optJSONArray("applications")

            val parsedApplications =
                mutableListOf<AdmissionRecord>()

            if (applicationsArray != null) {

                for (
                index in 0 until applicationsArray.length()
                ) {

                    val item =
                        applicationsArray.optJSONObject(index)
                            ?: continue

                    fun field(name: String): String {
                        val value = item.optString(name, "")
                        return if (value == "null") "" else value
                    }

                    parsedApplications.add(
                        AdmissionRecord(
                            id = item.optInt("id"),
                            applicationNo = field("application_no"),
                            admissionType = field("admission_type"),
                            academicYear = field("academic_year"),
                            applyingFor = field("applying_for"),
                            studentName = field("student_name"),
                            dob = field("dob"),
                            gender = field("gender"),
                            fatherName = field("father_name"),
                            motherName = field("mother_name"),
                            guardianPhone = field("guardian_phone"),
                            guardianEmail = field("guardian_email"),
                            address = field("address"),
                            previousInstitution = field("previous_institution"),
                            previousClass = field("previous_class"),
                            previousResult = field("previous_result"),
                            studentPhoto = field("student_photo"),
                            birthCertificate = field("birth_certificate"),
                            marksheet = field("marksheet"),
                            leavingCertificate = field("leaving_certificate"),
                            status = field("status"),
                            adminRemarks = field("admin_remarks"),
                            createdAt = field("created_at"),
                            updatedAt = field("updated_at")
                        )
                    )
                }
            }

            val pagination =
                json.optJSONObject("pagination")

            AdmissionsResult(
                success = true,
                message = json.optString(
                    "message",
                    "Admission applications loaded successfully."
                ),
                applications = parsedApplications,
                currentPage = pagination?.optInt(
                    "page",
                    page
                ) ?: page,
                totalPages = pagination?.optInt(
                    "total_pages",
                    1
                ) ?: 1
            )
        }

    } catch (e: Exception) {

        AdmissionsResult(
            success = false,
            message = "Could not connect to the Admissions server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}
 
// ---------------------------------------------------------
// CAREERS API
// ---------------------------------------------------------

private fun fetchCareers(): CareersResult {

    var connection: HttpURLConnection? = null

    return try {

        getSessionCookieManager()

        connection =
            URL(CAREERS_URL).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 20000
        connection.readTimeout = 20000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val responseCode = connection.responseCode

        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        if (responseBody.isBlank()) {
            throw Exception("Empty response from Careers server (HTTP $responseCode).")
        }

        val json = JSONObject(responseBody)

        if (
            responseCode !in 200..299 ||
            !json.optBoolean("success", false)
        ) {

            CareersResult(
                success = false,
                message = json.optString(
                    "message",
                    "Unable to load career applications."
                )
            )

        } else {

            val applicationsArray =
                json.optJSONArray("applications") ?: JSONArray()

            val parsedApplications =
                mutableListOf<CareerRecord>()

            for (index in 0 until applicationsArray.length()) {

                val item = applicationsArray.optJSONObject(index)
                    ?: continue

                fun field(name: String): String {
                    val value = item.optString(name, "")
                    return if (value == "null") "" else value
                }

                parsedApplications.add(
                    CareerRecord(
                        id = item.optInt("id"),
                        applicationNo = field("application_no"),
                        applicantName = field("applicant_name"),
                        email = field("email"),
                        phone = field("phone"),
                        positionApplied = field("position_applied"),
                        qualification = field("qualification"),
                        experienceYears = field("experience_years"),
                        currentAddress = field("current_address"),
                        coverLetter = field("cover_letter"),
                        resumePath = field("resume_path")
                    )
                )
            }

            CareersResult(
                success = true,
                message = json.optString(
                    "message",
                    "Career applications loaded successfully."
                ),
                applications = parsedApplications
            )
        }

    } catch (e: Exception) {

        CareersResult(
            success = false,
            message = "Could not connect to the Careers server: ${e.message}"
        )

    } finally {
        connection?.disconnect()
    }
}

// ---------------------------------------------------------
// CAREERS SCREEN
// ---------------------------------------------------------

@Composable
private fun CareersScreen(
    applications: List<CareerRecord>,
    isLoading: Boolean,
    message: String,
    onRefresh: () -> Unit,
    onOpenResume: (String) -> Unit
) {

    var selectedCareer by remember {
        mutableStateOf<CareerRecord?>(null)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val filteredApplications = applications.filter { item ->
        item.applicantName.contains(searchText, ignoreCase = true) ||
                item.applicationNo.contains(searchText, ignoreCase = true) ||
                item.positionApplied.contains(searchText, ignoreCase = true) ||
                item.phone.contains(searchText, ignoreCase = true) ||
                item.email.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Career Applications",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF4F6FA)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "View and review submitted job applications.",
            color = Color(0xFF9AA9BC),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search applications") },
            placeholder = {
                Text("Name, application no., position, phone...")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        RefreshButton(
            label = "Refresh Career Applications",
            onClick = onRefresh
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Total Applications: ${applications.size}",
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF4F6FA)
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                LoadingMessage("Loading career applications...")
            }

            message.isNotBlank() -> {
                ErrorMessage(message)
                RetryButton(onClick = onRefresh)
            }

            applications.isEmpty() -> {
                EmptyMessage("No career applications found.")
                RetryButton(onClick = onRefresh)
            }

            filteredApplications.isEmpty() -> {
                EmptyMessage("No applications match your search.")
            }

            else -> {
                filteredApplications.forEach { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable {
                                selectedCareer = item
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF172438)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 190.dp)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item.applicantName.ifBlank {
                                    "Unnamed Applicant"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF4F6FA)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.applicationNo.ifBlank {
                                    "Application #${item.id}"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF9AA9BC)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Position: ${item.positionApplied.ifBlank { "Not specified" }}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF4F6FA)
                            )

                            Text(
                                text = "Phone: ${item.phone.ifBlank { "Not provided" }}",
                                fontSize = 13.sp,
                                color = Color(0xFFD0D8E4)
                            )

                            Text(
                                text = "Qualification: ${item.qualification.ifBlank { "Not provided" }}",
                                fontSize = 13.sp,
                                color = Color(0xFFD0D8E4)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "VIEW FULL APPLICATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyugGold
                            )
                        }
                    }
                }
            }
        }

        selectedCareer?.let { item ->
            CareerDetailDialog(
                item = item,
                onDismiss = { selectedCareer = null },
                onOpenResume = onOpenResume
            )
        }
    }
}

// ---------------------------------------------------------
// CAREER DETAIL DIALOG
// ---------------------------------------------------------

@Composable
private fun CareerDetailDialog(
    item: CareerRecord,
    onDismiss: () -> Unit,
    onOpenResume: (String) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Application Details",
                color = Color(0xFFF4F6FA),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                )
            ) {
                CareerDetailRow("Application No.", item.applicationNo)
                CareerDetailRow("Applicant Name", item.applicantName)
                CareerDetailRow("Email", item.email)
                CareerDetailRow("Phone", item.phone)
                CareerDetailRow("Position Applied", item.positionApplied)
                CareerDetailRow("Qualification", item.qualification)
                CareerDetailRow(
                    "Experience",
                    if (item.experienceYears.isBlank()) {
                        "Not provided"
                    } else {
                        "${item.experienceYears} years"
                    }
                )
                CareerDetailRow("Current Address", item.currentAddress)
                CareerDetailRow("Cover Letter", item.coverLetter)

                if (item.resumePath.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            onOpenResume(item.resumePath)
                        }
                    ) {
                        Text(
                            text = "VIEW / DOWNLOAD RESUME",
                            color = Color(0xFFF4F6FA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    CareerDetailRow("Resume", "No resume uploaded")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = Color(0xFFFDC134))
            }
        }
    )
}

// ---------------------------------------------------------
// CAREER DETAIL ROW
// ---------------------------------------------------------

@Composable
private fun CareerDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF4F6FA)
        )

        Text(
            text = value.ifBlank { "Not provided" },
            color = Color(0xFFD0D8E4)
        )
    }
}
