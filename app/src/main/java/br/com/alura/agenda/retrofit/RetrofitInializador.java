package br.com.alura.agenda.retrofit;

import br.com.alura.agenda.retrofit.services.AlunoServices;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitInializador {

    private final Retrofit retrofit;

    public RetrofitInializador() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(interceptor);

        retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.106:8080/api/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client.build())
                .build();
    }

    public AlunoServices getAlunoService() {
        return retrofit.create(AlunoServices.class);
    }



}
