package cit.edu.vestil.worklog.data.api

import cit.edu.vestil.worklog.data.model.AdminPaymentRow
import cit.edu.vestil.worklog.data.model.AdminSummary
import cit.edu.vestil.worklog.data.model.AdminWorkLogRow
import cit.edu.vestil.worklog.data.model.AdminClientRow
import cit.edu.vestil.worklog.data.model.AdminCreateUserRequest
import cit.edu.vestil.worklog.data.model.AdminUpdateUserRequest
import cit.edu.vestil.worklog.data.model.AuditLogEntry
import cit.edu.vestil.worklog.data.model.ApiMessageResponse
import cit.edu.vestil.worklog.data.model.AuthResponse
import cit.edu.vestil.worklog.data.model.Client
import cit.edu.vestil.worklog.data.model.DashboardSummary
import cit.edu.vestil.worklog.data.model.ForgotPasswordRequest
import cit.edu.vestil.worklog.data.model.LoginRequest
import cit.edu.vestil.worklog.data.model.Payment
import cit.edu.vestil.worklog.data.model.RegisterRequest
import cit.edu.vestil.worklog.data.model.ResetPasswordRequest
import cit.edu.vestil.worklog.data.model.User
import cit.edu.vestil.worklog.data.model.WorkLog
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, String>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiMessageResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiMessageResponse>

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @GET("api/clients")
    suspend fun getClients(): Response<List<Client>>

    @POST("api/clients")
    suspend fun createClient(@Body client: Client): Response<Client>

    @PUT("api/clients/{id}")
    suspend fun updateClient(@Path("id") id: Long, @Body client: Client): Response<Client>

    @DELETE("api/clients/{id}")
    suspend fun deleteClient(@Path("id") id: Long): Response<Unit>

    @GET("api/worklogs")
    suspend fun getWorkLogs(): Response<List<WorkLog>>

    @POST("api/worklogs")
    suspend fun createWorkLog(@Body workLog: WorkLog): Response<WorkLog>

    @PUT("api/worklogs/{id}")
    suspend fun updateWorkLog(@Path("id") id: Long, @Body workLog: WorkLog): Response<WorkLog>

    @DELETE("api/worklogs/{id}")
    suspend fun deleteWorkLog(@Path("id") id: Long): Response<Unit>

    @GET("api/payments")
    suspend fun getPayments(): Response<List<Payment>>

    @POST("api/payments")
    suspend fun createPayment(@Body payment: Payment): Response<Payment>

    @PUT("api/payments/{id}")
    suspend fun updatePayment(@Path("id") id: Long, @Body payment: Payment): Response<Payment>

    @DELETE("api/payments/{id}")
    suspend fun deletePayment(@Path("id") id: Long): Response<Unit>

    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<User>>

    @POST("api/admin/users")
    suspend fun createAdminUser(@Body request: AdminCreateUserRequest): Response<User>

    @PUT("api/admin/users/{id}")
    suspend fun updateAdminUser(@Path("id") id: Long, @Body request: AdminUpdateUserRequest): Response<User>

    @POST("api/admin/users/{id}/reset-password")
    suspend fun resetAdminUserPassword(@Path("id") id: Long): Response<String>

    @POST("api/admin/users/{id}/toggle-status")
    suspend fun toggleAdminUserStatus(@Path("id") id: Long): Response<User>

    @GET("api/admin/summary")
    suspend fun getAdminSummary(): Response<AdminSummary>

    @GET("api/admin/worklogs")
    suspend fun getAdminWorkLogs(): Response<List<AdminWorkLogRow>>

    @PUT("api/admin/worklogs/{id}/approve")
    suspend fun approveAdminWorkLog(@Path("id") id: Long): Response<AdminWorkLogRow>

    @PUT("api/admin/worklogs/{id}/reject")
    suspend fun rejectAdminWorkLog(@Path("id") id: Long): Response<AdminWorkLogRow>

    @GET("api/admin/clients")
    suspend fun getAdminClients(): Response<List<AdminClientRow>>

    @PUT("api/admin/clients/{id}/archive")
    suspend fun archiveAdminClient(@Path("id") id: Long): Response<AdminClientRow>

    @GET("api/admin/payments")
    suspend fun getAdminPayments(): Response<List<AdminPaymentRow>>

    @PUT("api/admin/payments/{id}/approve")
    suspend fun approveAdminPayment(@Path("id") id: Long): Response<AdminPaymentRow>

    @PUT("api/admin/payments/{id}/reject")
    suspend fun rejectAdminPayment(@Path("id") id: Long): Response<AdminPaymentRow>

    @GET("api/admin/audit-logs")
    suspend fun getAuditLogs(): Response<List<AuditLogEntry>>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>
}
