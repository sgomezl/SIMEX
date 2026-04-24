package com.mygdx.primelogistics.android.api

import com.mygdx.primelogistics.android.models.LoginRequest
import com.mygdx.primelogistics.android.models.LoginResponse
import com.mygdx.primelogistics.android.models.RejectOperationRequest
import com.mygdx.primelogistics.android.models.UpdateIdentificationCardPathRequest
import com.mygdx.primelogistics.android.models.Operation
import com.mygdx.primelogistics.android.models.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<User>

    @PUT("api/auth/id-card-path")
    suspend fun updateIdentificationCardPath(
        @Body request: UpdateIdentificationCardPathRequest
    ): Response<ResponseBody>

    @GET("api/operations/user-operations")
    suspend fun getUserOperations(): Response<List<Operation>>

    @GET("api/operations/recent")
    suspend fun getRecentUserOperations(): Response<List<Operation>>

    @POST("api/operations/{operationId}/reject")
    suspend fun rejectOperation(
        @Path("operationId") operationId: Int,
        @Body request: RejectOperationRequest
    ): Response<ResponseBody>

    @POST("api/operations/{operationId}/accept")
    suspend fun acceptOperation(
        @Path("operationId") operationId: Int
    ): Response<ResponseBody>
}
