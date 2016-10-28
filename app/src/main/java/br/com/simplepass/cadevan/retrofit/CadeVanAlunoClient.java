package br.com.simplepass.cadevan.retrofit;

import java.util.List;
import java.util.Map;

import br.com.simplepass.cadevan.domain.Driver;
import br.com.simplepass.cadevan.domain.User;
import br.com.simplepass.cadevan.domain.Van;
import br.com.simplepass.cadevan.dto.RecoverPasswordBean;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by leandro on 3/7/16.
 */
public interface CadeVanAlunoClient {
    @POST("users")
    Call<User> register(@Body User user);

    @PUT("users/{id}")
    Call<Void> updateUser(@Body User user, @Path("id") long id);

    @GET("users")
    Call<List<User>> getUsersByPhoneNumber(@Query("phoneNumber") String phone);

    @GET("drivers")
    Call<List<Driver>> getDrivers();

    @GET("vans")
    Call<Map<Integer, Van>> getAllVans();

    @GET("vans/{id}")
    Call<Van> getVanById(@Path("id") long id);

    @POST("users/recoverPassword")
    Call<User> recoverPassword(@Body RecoverPasswordBean recoverPasswordBean);

    @POST("oauth/token")
    @FormUrlEncoded
    Call<OAuthTokenResponse> getAuthToken(@Field("username") String username,
                                          @Field("password") String password,
                                          @Field("grant_type") String grantType);
}
