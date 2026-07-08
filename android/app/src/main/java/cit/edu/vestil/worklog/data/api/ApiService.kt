package cit.edu.vestil.worklog.data.api

import cit.edu.vestil.worklog.data.model.AuthResponse
import cit.edu.vestil.worklog.data.model.Client
import cit.edu.vestil.worklog.data.model.DashboardSummary
import cit.edu.vestil.worklog.data.model.LoginRequest
import cit.edu.vestil.worklog.data.model.Payment
import cit.edu.vestil.worklog.data.model.RegisterRequest
import cit.edu.vestil.worklog.data.model.User
import cit.edu.vestil.worklog.data.model.WorkLog
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, String>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @GET("api/clients")
    suspend fun getClients(): Response<List<Client>>

    @POST("api/clients")
    suspend fun createClient(@Body client: Client): Response<Client>

    @GET("api/worklogs")
    suspend fun getWorkLogs(): Response<List<WorkLog>>

    @POST("api/worklogs")
    suspend fun createWorkLog(@Body workLog: WorkLog): Response<WorkLog>

    @GET("api/payments")
    suspend fun getPayments(): Response<List<Payment>>

    @POST("api/payments")
    suspend fun createPayment(@Body payment: Payment): Response<Payment>

    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("api/admin/summary")
    suspend fun getAdminSummary(): Response<Map<String, Any>>
}
