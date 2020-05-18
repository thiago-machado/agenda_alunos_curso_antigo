package br.com.alura.agenda.retrofit.services;

import java.util.List;

import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.modelo.dto.AlunosSync;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AlunoServices {

    @POST("aluno")
    Call<Void> insere(@Body Aluno aluno);

    @GET("aluno")
    Call<AlunosSync> lista();
}
