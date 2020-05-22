package br.com.alura.agenda.firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import br.com.alura.agenda.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * O primeiro passo é criar um projeto dentro do firebase para que possamos utilizar
 * o cloud messaging. Sendo assim, precisamos fazer login em nossa conta do Google e
 * depois ir até a página do firebase em https://console.firebase.google.com/ e
 * clicar Criar novo projeto.
 *
 * ATENÇÃO: é necessário acessar a área de configurações do FIREBASE, copiar a chave
 * autorizadora na aba Cloud Messaging e colar na aplicação WEB.
 * Como a aplicação WEB se comunicará com o servidor quando um novo aluno for cadastrado,
 * é necessário que o app WEB tenha a chave válida para se comunicar com o Firebase.
 *
 * Push Notification: servidor envia requisições para o cliente.
 *
 * Firebase: é uma plataforma do Google que oferece diversos serviços,
 * entre eles o Cloud Messaging.
 *
 * Para baixar o servidor em ZIP:
 * https://github.com/alura-cursos/android-sync-parte-1/archive/servidor.zip
 *
 * Para acessar o servidor no GITHUB:
 * https://github.com/alura-cursos/android-sync-parte-1
 *
 */
public class AgendaMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
        Existem muitas possibilidades para o tratamento da mensagem, mas o
        nosso servidor envia as informações por meio do data que é um Map
        de chave e valor.
         */
        Map<String, String> mensagem = remoteMessage.getData();
        Log.i("firebase_log", String.valueOf(mensagem));
    }

    /**
     * O método onTokenRefresh só será chamado uma vez no ciclo de vida da aplicação,
     * porque não faz sentido gerar token com frequência. Para que consigamos gerar um novo,
     * temos algumas opções: desinstalar a aplicação, limpar os dados da mesma ou instalar
     * ela em outro aparelho.
     */
    @Override
    public void onNewToken(String token) {
        Log.d("firebase_log", "Refreshed token: " + token);

        /*
        Esse token é importante porque é por meio dele que o firebase enviará as
        informações vindas do servidor.
         */
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Agora que o nosso projeto contém todas as configurações e dependências necessárias
     * para utilizarmos o FCM, precisamos realizar o primeiro passo que é justamente gerar o
     * token identificador, pois dessa forma o FCM criará o vínculo (conexão) com o
     * dispositivo para que seja possível enviar as mensagens!
     *
     * @param token
     */
    private void sendRegistrationToServer(String token) {
        Call<Void> call = new RetrofitInicializador().getDispositivoService().enviaToken(token);

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.i("token enviado", token);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
